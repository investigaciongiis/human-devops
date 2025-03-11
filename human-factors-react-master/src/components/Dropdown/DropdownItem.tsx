import "./DropdownItem.css";

export const DropdownItem = (props : any) => {
  return (
      <a
        className="menu-item"
        href={props.href}
        onClick={props.onClick}
      >
        <span className="icon-button">{props.leftIcon}</span>
        {props.children}
      </a>
  );
};
