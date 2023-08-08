import classes from "./ProfileMain.module.css";

import ProfileCalender from "../../components/Profile/ProfileCalender";

function ProfileMain(props) {
  return (
    <div className="ProfileMain">
      <div className={classes.ProfileCalender}>
        <ProfileCalender />
      </div>
      <br />
      <div className={classes.ProfileCustom}>
        {/* menu에서 받아온 정보 출력 */}
        {props.Menu}
      </div>
    </div>
  );
}

export default ProfileMain;