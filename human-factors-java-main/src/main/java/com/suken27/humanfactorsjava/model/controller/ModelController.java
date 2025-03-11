package com.suken27.humanfactorsjava.model.controller;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;

import com.slack.api.bolt.App;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.block.LayoutBlock;
import com.suken27.humanfactorsjava.model.Action;
import com.suken27.humanfactorsjava.model.HumanFactorFactory;
import com.suken27.humanfactorsjava.model.Question;
import com.suken27.humanfactorsjava.model.Team;
import com.suken27.humanfactorsjava.model.TeamHumanFactor;
import com.suken27.humanfactorsjava.model.TeamManager;
import com.suken27.humanfactorsjava.model.TeamMember;
import com.suken27.humanfactorsjava.model.User;
import com.suken27.humanfactorsjava.model.dto.ActionDto;
import com.suken27.humanfactorsjava.model.dto.HumanFactorDto;
import com.suken27.humanfactorsjava.model.dto.QuestionDto;
import com.suken27.humanfactorsjava.model.dto.TeamDto;
import com.suken27.humanfactorsjava.model.dto.TeamManagerDto;
import com.suken27.humanfactorsjava.model.dto.UserDto;
import com.suken27.humanfactorsjava.model.exception.EmailInUseException;
import com.suken27.humanfactorsjava.model.exception.HumanFactorNotFoundException;
import com.suken27.humanfactorsjava.model.exception.IncorrectLoginException;
import com.suken27.humanfactorsjava.model.exception.MemberAlreadyInTeamException;
import com.suken27.humanfactorsjava.model.exception.QuestionNotFoundException;
import com.suken27.humanfactorsjava.model.exception.TeamManagerNotFoundException;
import com.suken27.humanfactorsjava.model.exception.TeamMemberNotFoundException;
import com.suken27.humanfactorsjava.model.scheduling.ScheduleController;
import com.suken27.humanfactorsjava.repository.QuestionRepository;
import com.suken27.humanfactorsjava.repository.TeamManagerRepository;
import com.suken27.humanfactorsjava.repository.TeamMemberRepository;
import com.suken27.humanfactorsjava.repository.TeamRepository;
import com.suken27.humanfactorsjava.rest.exception.MemberInAnotherTeamException;
import com.suken27.humanfactorsjava.slack.SlackBlockBuilder;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class ModelController {

	// TODO: Refactor model to decouple the messaging logic from the model
	// TODO: Cache users to avoid fetching them every time

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private TeamManagerRepository teamManagerRepository;

	@Autowired
	private HumanFactorFactory humanFactorFactory;

	@Autowired
	private TeamRepository teamRepository;

	@Autowired
	private TeamMemberRepository teamMemberRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private ScheduleController scheduleController;

	@Autowired
	private SlackBlockBuilder slackBlockBuilder;

	@Autowired
	@Lazy
	private App slackApp;

	public void checkUser(String email, String password) {
		TeamManager user = teamManagerRepository.findByEmail(email);
		if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
			throw new IncorrectLoginException();
		}
	}

	public TeamManagerDto getTeamManager(String email) {
		TeamManager teamManager = teamManagerRepository.findByEmail(email);
		if (teamManager == null) {
			throw new TeamManagerNotFoundException(email);
		}
		return new TeamManagerDto(teamManager);
	}

	public TeamManagerDto registerTeamManager(String email, String password)
			throws SchedulerException {
		if (teamManagerRepository.findByEmail(email) != null) {
			throw new EmailInUseException(email);
		}
		TeamManager entity = new TeamManager(humanFactorFactory);
		entity.setEmail(email);
		entity.setPassword(passwordEncoder.encode(password));
		scheduleQuestions(entity.getTeam());
		return new TeamManagerDto(teamManagerRepository.save(entity));
	}

	public TeamManagerDto updateTeamManagerSlackId(String email, String slackId) {
		TeamManager teamManager = teamManagerRepository.findByEmail(email);
		if (teamManager == null) {
			throw new TeamManagerNotFoundException(email);
		}
		teamManager.setSlackId(slackId);
		return new TeamManagerDto(teamManagerRepository.save(teamManager));
	}

	public TeamManagerDto updateTeamManagerPassword(String email, String oldPassword, String newPassword) {
		TeamManager teamManager = teamManagerRepository.findByEmail(email);
		if (teamManager == null) {
			throw new TeamManagerNotFoundException(email);
		}
		if (!passwordEncoder.matches(oldPassword, teamManager.getPassword())) {
			throw new IncorrectLoginException();
		}
		teamManager.setPassword(passwordEncoder.encode(newPassword));
		return new TeamManagerDto(teamManagerRepository.save(teamManager));
	}

	public TeamDto getTeam(String teamManagerEmail) {
		Team team = teamRepository.findByTeamManagerEmail(teamManagerEmail);
		if (team == null) {
			return null;
		}
		return new TeamDto(team);
	}

	public TeamDto updateTeam(TeamDto teamDto)
			throws SchedulerException {
		Team team = teamRepository.findByTeamManagerId(teamDto.getManager());
		if (team == null) {
			throw new TeamManagerNotFoundException(teamDto.getManager());
		}
		// parse questionSendingTime to LocalTime
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
		team.setZonedQuestionSendingTime(LocalTime.parse(teamDto.getQuestionSendingTime(), dateTimeFormatter));
		team.setQuestionsPerDay(teamDto.getQuestionsPerDay());
		team.setSlackBotToken(teamDto.getSlackBotToken());
		scheduleQuestions(team);
		return new TeamDto(teamRepository.save(team));
	}

	public TeamDto addTeamMember(String teamManagerEmail, String email, String slackId)
			throws MemberAlreadyInTeamException, MemberInAnotherTeamException {
		Team team = teamRepository.findByTeamManagerEmail(teamManagerEmail);
		// Team should not be null as every team manager is created with an empty team,
		// so no check should be required
		if (teamManagerEmail.equals(email) || team.isMember(email)) {
			throw new MemberAlreadyInTeamException(email);
		}
		TeamMember teamMember = teamMemberRepository.findByEmail(email);
		if (teamMember != null) {
			throw new MemberInAnotherTeamException(email);
		}
		teamMember = new TeamMember(humanFactorFactory);
		teamMember.setEmail(email);
		teamMember.setTeam(team);
		teamMember.setSlackId(slackId);
		team.addMember(teamMember);
		return new TeamDto(teamRepository.save(team));
	}

	public TeamDto removeTeamMember(String teamManagerEmail, String email) {
		Team team = teamRepository.findByTeamManagerEmail(teamManagerEmail);
		// Team should not be null as every team manager is created with an empty team,
		// so no check should be required
		if (!team.isMember(email)) {
			throw new TeamMemberNotFoundException(email);
		}
		// At this point the user should exist, this is a double check just in case
		TeamMember teamMember = teamMemberRepository.findByEmail(email);
		if (teamMember == null) {
			throw new TeamMemberNotFoundException(email);
		}
		team.removeMember(teamMember);
		return new TeamDto(teamRepository.save(team));
	}

	public TeamDto modifyQuestionSendingTime(String teamManagerEmail, LocalTime questionSendingTime)
			throws SchedulerException {
		log.debug("Modifying question sending time to [{}] for team managed by [{}]", questionSendingTime,
				teamManagerEmail);
		Team team = teamRepository.findByTeamManagerEmail(teamManagerEmail);
		team.setZonedQuestionSendingTime(questionSendingTime);
		scheduleQuestions(team);
		return new TeamDto(teamRepository.save(team));
	}

	public Map<UserDto, List<QuestionDto>> launchQuestions(String teamManagerEmail) {
		Team team = teamRepository.findByTeamManagerEmail(teamManagerEmail);
		Map<User, List<Question>> questions = team.launchQuestions();
		Map<UserDto, List<QuestionDto>> questionsDto = new HashMap<>();
		for (Entry<User, List<Question>> entry : questions.entrySet()) {
			questionsDto.put(new UserDto(entry.getKey()), QuestionDto.toDto(entry.getValue()));
		}
		return questionsDto;
	}

	public String answerQuestion(String userEmail, Long questionId, Double answer) {
		Team team = teamRepository.findByMemberEmail(userEmail);
		if(team == null) {
			throw new TeamMemberNotFoundException(userEmail);
		}
		Optional<Question> optional = questionRepository.findById(questionId);
		if(!optional.isPresent()) {
			throw new QuestionNotFoundException(questionId);
		}
		String text = team.answerQuestion(userEmail, optional.get(), answer);
		teamRepository.save(team);
		return text;
	}

	public void pushQuestionsToSlack(Team team) throws IOException, SlackApiException {
		Map<UserDto, List<QuestionDto>> questions = launchQuestions(team.getManager().getEmail());
		for (Entry<UserDto, List<QuestionDto>> entry : questions.entrySet()) {
			log.debug("Sending questions to user [{}]", entry.getKey().getSlackId());
			for (List<LayoutBlock> blocks : slackBlockBuilder.questionBlocks(entry.getValue())) {
				slackApp.client().chatPostMessage(r -> {
					r.channel(entry.getKey().getSlackId());
					r.blocks(blocks);
					r.token(team.getSlackBotToken());
					r.text("Human factors daily questions");
					return r;
				});
			}
		}
	}

	/**
	 * Returns the recommended actions for the team managed by the given team manager.
	 * @param teamManagerEmail Email of the team manager.
	 * @return List of recommended actions.
	 */
	public List<ActionDto> getRecommendedActions(String teamManagerEmail) {
		Team team = teamRepository.findByTeamManagerEmail(teamManagerEmail);
		if(team == null) {
			throw new TeamManagerNotFoundException(teamManagerEmail);
		}
		Map<Action, Double> actions = team.getRecommendedActions();
		List<ActionDto> actionsDto = new ArrayList<>();
		for(Entry<Action, Double> entry : actions.entrySet()) {
			ActionDto actionDto = new ActionDto();
			actionDto.setId(entry.getKey().getId());
			actionDto.setTitle(entry.getKey().getType().getTitle());
			actionDto.setDescription(entry.getKey().getType().getDescription());
			actionDto.setScore(entry.getValue());
			actionsDto.add(actionDto);
		}
		return actionsDto;
	}

	public List<HumanFactorDto> getAllHumanFactors(String teamManagerEmail) {
		Team team = teamRepository.findByTeamManagerEmail(teamManagerEmail);
		if(team == null) {
			throw new TeamManagerNotFoundException(teamManagerEmail);
		}
		return HumanFactorDto.toDto(new ArrayList<>(team.getHumanFactors().values()));
	}

	public HumanFactorDto getHumanFactor(String teamManagerEmail, Long humanFactorId) {
		Team team = teamRepository.findByTeamManagerEmail(teamManagerEmail);
		if(team == null) {
			throw new TeamManagerNotFoundException(teamManagerEmail);
		}
		for(TeamHumanFactor humanFactor : team.getHumanFactors().values()) {
			if(humanFactor.getId().equals(humanFactorId)) {
				return new HumanFactorDto(humanFactor);
			}
		}
		throw new HumanFactorNotFoundException(humanFactorId);
	}

	private void scheduleQuestions(Team team) throws SchedulerException {
		String cronExpression = "0 " + team.getQuestionSendingTime().getMinute() + " "
				+ team.getQuestionSendingTime().getHour() + " ? * MON-FRI";
		scheduleController.scheduleJob("QuestioningJob" + team.getId(), cronExpression, this, slackApp,
				slackBlockBuilder, new TeamDto(team), team.getManager().getEmail());
	}

}
