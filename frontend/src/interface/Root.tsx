import { Outlet } from "react-router-dom";
import { DropdownMenu } from "../components/Dropdown/DropdownMenu";
import { NavItem } from "../components/Navbar/NavItem";
import { Navbar } from "../components/Navbar/Navbar";
import "./Root.css";
import { SlideBar } from "./SlideBar";

function Root() {

	const { default: user } = require("../svg/user.svg") as  { default: string };

	return (
		<div className="Root">
			<Navbar>
				<NavItem icon={user}>
					<DropdownMenu />
				</NavItem>
			</Navbar>
			<div className="Root-belowNavBar">
				<SlideBar />
				<div className="Root-content">
					<Outlet />
				</div>
			</div>
		</div>
	);
}

export default Root;
