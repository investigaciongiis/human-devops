package com.suken27.humanfactorsjava.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Entity
@Data
@EqualsAndHashCode(exclude = { "manager", "members", "humanFactors" })
@Slf4j
public class Team {

	@Id
	@GeneratedValue
	private Long id;

	@OneToOne(mappedBy = "team")
	private TeamManager manager;

	@OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
	private List<TeamMember> members;
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@ElementCollection
	private Map<HumanFactorType, TeamHumanFactor> humanFactors;
	// TODO: Hash code could be the same for two different users, another value
	// should be used for the map key
	/**
	 * This map is a flattened nested map that represents a Map<HumanFactorType,
	 * Map<User, Double>>.
	 * The String key is built using HumanFactorType.getId() + "." +
	 * User.hashCode().
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	private Map<String, Double> humanFactorUserScores;
	@JsonFormat(pattern = "HH:mm")
	private LocalTime questionSendingTime;
	private ZoneId timeZone;
	private int questionsPerDay;
	// TODO: Refactor to avoid coupling between the model and a specific messaging
	// interface
	private String slackBotToken;

	/**
	 * This constructor should never be used. Use Team(TeamManager) instead.
	 */
	public Team() {
		super();
	}

	public Team(TeamManager teamManager, List<TeamHumanFactor> humanFactors) {
		super();
		manager = teamManager;
		members = new ArrayList<>();
		// This sets the question sending time to 9:00 spanish time.
		questionSendingTime = LocalTime.of(9, 0);
		timeZone = ZoneId.of("Europe/Madrid");
		setZonedQuestionSendingTime(questionSendingTime);
		questionsPerDay = 10;
		this.humanFactors = new HashMap<>();
		for (TeamHumanFactor teamHumanFactor : humanFactors) {
			this.humanFactors.put(teamHumanFactor.getType(), teamHumanFactor);
		}
		humanFactorUserScores = new HashMap<>();
		for (TeamHumanFactor teamHumanFactor : humanFactors) {
			String teamHumanFactorId = teamHumanFactor.getType().getId().toString();
			if (teamHumanFactor.getType().isMemberMeasured()) {
				for (User user : members) {
					humanFactorUserScores.put(teamHumanFactorId + "." + user.hashCode(), null);
				}
			}
			humanFactorUserScores.put(teamHumanFactorId + "." + manager.hashCode(), null);
		}
	}

	/**
	 * Gets the question sending time in the team's time zone. The time returned
	 * will be in the team's time zone.
	 * 
	 * @return Time of the day to send the questions. This time is according the
	 *         team's time zone.
	 */
	public LocalTime getZonedQuestionSendingTime() {
		return LocalDateTime.of(LocalDate.now(), questionSendingTime).atZone(ZoneId.systemDefault())
				.withZoneSameInstant(timeZone).toLocalTime();
	}

	/**
	 * Sets the question sending time in the team's time zone. The time received
	 * should be in the team's time zone and the time stored will be in the system's
	 * time zone.
	 * 
	 * @param questionSendingTime Time of the day to send the questions. This time
	 *                            should be according the team's time zone.
	 */
	public void setZonedQuestionSendingTime(LocalTime questionSendingTime) {
		ZonedDateTime zonedTime = LocalDateTime.of(LocalDate.now(), questionSendingTime).atZone(timeZone);
		this.questionSendingTime = zonedTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalTime();
	}

	public void addMember(TeamMember member) {
		members.add(member);
		for (Entry<HumanFactorType, TeamHumanFactor> entry : humanFactors.entrySet()) {
			if (entry.getKey().isMemberMeasured()) {
				humanFactorUserScores.put(entry.getKey() + "." + member.hashCode(), null);
				updateHumanFactorScores(entry.getKey(), null);
			}
		}
	}

	/**
	 * Removes member from the team, marks it as a deleted team member, and sets the
	 * time of deletion.
	 * 
	 * @param member Member to remove from the team.
	 */
	public void removeMember(TeamMember member) {
		members.remove(member);
		for (Entry<HumanFactorType, TeamHumanFactor> entry : humanFactors.entrySet()) {
			if (entry.getKey().isMemberMeasured()) {
				humanFactorUserScores.remove(entry.getKey() + "." + member.hashCode(), null);
				updateHumanFactorScores(entry.getKey(), null);
			}
		}
		member.setDeleted(true);
		member.setDeletionTime(LocalDateTime.now());
		member.setTeam(null);
	}

	public boolean isMember(TeamMember member) {
		return members.contains(member);
	}

	public boolean isMember(String email) {
		return members.stream().anyMatch(member -> member.getEmail().equals(email));
	}

	public void setTimeZone(ZoneId timeZone) {
		this.timeZone = timeZone;
		ZonedDateTime zonedTime = LocalDateTime.of(LocalDate.now(), questionSendingTime).atZone(timeZone);
		questionSendingTime = zonedTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalTime();
	}

	public Map<User, List<Question>> launchQuestions() {
		Map<User, List<Question>> questionMap = new HashMap<>();
		for (User user : getAllMembers()) {
			questionMap.put(user, user.launchQuestions(questionsPerDay));
		}
		return questionMap;
	}

	/**
	 * Gets the recommended actions for the team. Only actions for fully measured
	 * human factors are included.
	 * 
	 * @return Map of recommended actions and their score. The score is a value
	 *         between 0.0 and 1.0 that measures how much the action is recommended,
	 *         being 1.0 the maximum recommendation. This score is the inverse of
	 *         the score of the human factor associated with the action.
	 */
	public Map<Action, Double> getRecommendedActions() {
		Map<Action, Double> recommendedActions = new HashMap<>();
		for (TeamHumanFactor humanFactor : humanFactors.values()) {
			if (humanFactor.getScore() != null) {
				for (Action action : humanFactor.getActions()) {
					// If the action has already been recommended, the score is the maximum between all the scores calculated.
					if(recommendedActions.get(action) != null) {
						recommendedActions.put(action, Math.max(recommendedActions.get(action), 1 - humanFactor.getScore()));
					} else {
						recommendedActions.put(action, 1 - humanFactor.getScore());
					}
				}
			}
		}
		return recommendedActions;
	}

	/**
	 * Answers the question for the given user and returns the answer in text.
	 * 
	 * @param userEmail Email of the user that answers the question.
	 * @param question  Question to answer.
	 * @param answer    Answer to the question (value between 0.0 and 1.0)
	 * @return Answer to the question in text.
	 * @see Question
	 */
	public String answerQuestion(String userEmail, Question question, Double answer) {
		User user = getMember(userEmail);
		HumanFactor humanFactor = user.answerQuestion(question, answer);
		if (humanFactor != null) {
			String key = humanFactor.getType().getId() + "." + user.hashCode();
			humanFactorUserScores.put(key, humanFactor.getScore());
			updateHumanFactorScores(humanFactor.getType(), null);
		}
		return question.answerValueToText(answer);
	}

	private User getMember(String memberEmail) {
		for (User user : getAllMembers()) {
			if (user.getEmail().equals(memberEmail)) {
				return user;
			}
		}
		return null;
	}

	private List<User> getAllMembers() {
		List<User> allMembers = new ArrayList<>(this.members);
		allMembers.add(manager);
		return allMembers;
	}

	private Double updateHumanFactorScores(HumanFactorType humanFactorType, HumanFactorType origin) {
		Double average = updateHumanFactorUserScores(humanFactorType);
		if (average == null) {
			return null;
		}
		TeamHumanFactor teamHumanFactor = humanFactors.get(humanFactorType);
		int humanFactorScoresCounter = 1;
		for (TeamHumanFactor affectedBy : teamHumanFactor.getAffectedBy()) {
			if (affectedBy.getType() != origin) {
				// Precondition: No circular dependencies between human factors
				Double affectedByScore = updateHumanFactorScores(affectedBy.getType(), humanFactorType);
				// If not all the depending factors are available, the human factor score is
				// still calculated
				if (affectedByScore != null) {
					average += affectedByScore;
					humanFactorScoresCounter++;
				}
			}
		}
		// Every dependant human factor has the same weight in the average than the
		// human factor being calculated
		average /= humanFactorScoresCounter;
		humanFactors.get(humanFactorType).setScore(average);
		// Update the scores of the human factors that depend on this one
		for (TeamHumanFactor affectsTo : teamHumanFactor.getAffectsTo()) {
			if (affectsTo.getType() != origin) {
				// Precondition: No circular dependencies between human factors
				updateHumanFactorScores(affectsTo.getType(), humanFactorType);
			}
		}
		return average;
	}

	/**
	 * Updates the scores of the users for the given human factor type.
	 * 
	 * @param humanFactorType Human factor type to update the scores.
	 * @return Average score of the users for the given human factor type. If the
	 *         score cannot be calculated, null is returned. If there is no
	 *         questions to calculate the score, 0.0 is returned.
	 */
	private Double updateHumanFactorUserScores(HumanFactorType humanFactorType) {
		Double average = 0.0;
		int membersCounter = 1;
		// Check if the human factor has questions (if it has no questions, then the
		// score is calculated using the depending factors)
		if (humanFactorType.getQuestionTypes().isEmpty()) {
			return average;
		}
		String key;
		Double userScore;
		if (humanFactorType.isMemberMeasured()) {
			for (User user : members) {
				key = humanFactorType.getId() + "." + user.hashCode();
				userScore = humanFactorUserScores.get(key);
				if (userScore == null) {
					humanFactorUserScores.put(key, null);
					return null;
				}
				average += userScore;
				membersCounter++;
			}
		}
		key = humanFactorType.getId() + "." + manager.hashCode();
		userScore = humanFactorUserScores.get(key);
		if (userScore == null) {
			humanFactorUserScores.put(key, null);
			return null;
		}
		average += userScore;
		average /= membersCounter;
		return average;
	}

}
