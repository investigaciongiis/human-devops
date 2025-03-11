import { useState } from "react";
import { HiOutlineCog } from "react-icons/hi";
import { IoIosLogOut } from "react-icons/io";
import { CSSTransition } from "react-transition-group";
import AuthService from "../../authentication/AuthService";
import { DropdownItem } from "./DropdownItem";
import "./DropdownMenu.css";

function handleLogout(event : React.MouseEvent<HTMLAnchorElement, MouseEvent>) {
	event.preventDefault();
	AuthService.logout();
}

export const DropdownMenu = () => {
	// state for csstransition
	const [active, setActive] = useState("main");
	const [menuHeight, setMenuHeight] = useState<number>();

	/*we use this function as a callback in CSSTransition onEnter prop which runs this callback when it is
  mounted to DOM
  */

	function calcHeight(el : HTMLElement) {
		// el.offsetHeight is height in pixels of that component. we use this in dropdown menu style height to set height
		const height = el.offsetHeight;
		console.log(height);
		setMenuHeight(height);
	}

	return (
		<div className="dropdown" style={{ height : menuHeight }}>
			{/* 
There are two dropdown containers for csstransitions component main and secondary.
we always go back to main container and we use secondary as name for more container because we can style easy
    */}
			<CSSTransition
				// if in is true then this CSSTransition component renders
				in={active === "main"}
				unmountOnExit
				timeout={500}
				classNames="menu-primary"
				onEnter={calcHeight}
			>
				{/* CSSTransition component check for next element and adds transitions to that element by adding classNames we specified
        in this component props to next element and we add css to animate
        */}
				<div className="menu">
					{/* if this item is clicked then only CSSTransition component will be triggered if active === settings as given in in prop boolean */}
					<DropdownItem
						leftIcon={<HiOutlineCog />}
						href="/settings"
					>
						Settings
					</DropdownItem>
					<DropdownItem 
						leftIcon={<IoIosLogOut />}
						href="/login"
						onClick={handleLogout}
					>
						Log Out
					</DropdownItem>
				</div>
			</CSSTransition>

		</div>
	);
};