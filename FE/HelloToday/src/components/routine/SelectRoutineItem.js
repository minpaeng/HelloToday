import classes from "./SelectRoutineItem.module.css";
import { SplideSlide } from "@splidejs/react-splide";
import { useDispatch } from "react-redux";
import { useState } from "react";
import { addRoutine, deleteRoutine } from "../../store/SelectRoutineSlice";
import classNames from "classnames";
import Swal from "sweetalert2";

function SelectRoutineItem({
  routineId,
  routineContent,
  routineImg,
  isModalOpen,
  updateSelectedCount,
  selectedCount,
  isLogin,
}) {
  const [selected, setSelected] = useState(false);
  const dispatch = useDispatch();

  const onClick = () => {
    if (!isLogin) {
      return;
    }

    if (isModalOpen) {
      return;
    }

    if (!selected) {
      if (selectedCount < 5) {
        dispatch(addRoutine({ routineId, routineContent, routineImg }));
        updateSelectedCount(selectedCount + 1);
        setSelected(!selected);
      } else {
        Swal.fire({
          icon: "warning",
          title: "루틴 선택",
          text: "루틴 선택은 5개까지 가능합니다.",
          confirmButtonText: "확인"
        })
      }
    } else {
      dispatch(deleteRoutine({ routineId }));
      updateSelectedCount(selectedCount - 1);
      setSelected(!selected);
    }
  };

  const isSelectedStyle = classNames({
    [classes.modalStyle]: isModalOpen,
    [classes.selectedsplideSlide]: selected && !isModalOpen,
    [classes.unselectedSplideSlide]: !selected && !isModalOpen,
  });

  return (
    <SplideSlide className={isSelectedStyle} onClick={onClick}>
      <img
        className={classes.routineImg}
        src={`images/Routine/${routineImg}`}
        alt="routineImg"
      />
      <div className={classes.routineName}>{routineContent}</div>
    </SplideSlide>
  );
}

export default SelectRoutineItem;
