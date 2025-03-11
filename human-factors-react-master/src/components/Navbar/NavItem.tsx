import { useState } from "react";
import "./navItem.css";

export const NavItem = (props : any) => {
  const [open, setOpen] = useState(false);

  return (
    <li className="nav-item">
      <a className="icon-button" onClick={() => setOpen(!open)}>
        <img className="icon-button-image" src={props.icon}/>
      </a>
      {open && props.children}
    </li>
  );
};
