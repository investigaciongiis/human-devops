import "./SlideBar.css";

export const SlideBar = () => {

	const {default: graph} = require("../svg/graph.svg") as {default: string};
	const {default: users} = require("../svg/users.svg") as {default: string};
	const {default: warning} = require("../svg/warning.svg") as {default: string};

	return (
		<div className="SlideBar">
			<a href="/graph" className="SlideBar-button">
				<img
					src={graph}
					className="SlideBar-button-icon"
					alt="Human factors graph"
				/>
				<div className="SlideBar-button-text">Graph</div>
			</a>
			<a href="/team" className="SlideBar-button">
				<img
					src={users}
					className="SlideBar-button-icon"
					alt="Team management"
				/>
				<div className="SlideBar-button-text">Team</div>
			</a>
			<a href="/actions" className="SlideBar-button">
				<img
					src={warning}
					className="SlideBar-button-icon"
					alt="Actions page"
				/>
				<div className="SlideBar-button-text">Actions</div>
			</a>
		</div>
	);
}
