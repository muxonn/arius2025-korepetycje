import { API } from "../services/api";

export const util = {
  getSubjectNamesFromIdString: (subjectIdsString) => {
    const subjectIds = subjectIdsString.replace(/{|}/g, "").split(",").map(Number).sort(); // Usuwa klamry { } i dzieli na tablice
    const subjectNames = subjectIds.map((id) => API.getSubjectNameById(id));
    return subjectNames;
  },
  
  getDifficultyNamesFromIdString: (difficultyIdsString) => {
    const difficultyIds = difficultyIdsString.replace(/{|}/g, "").split(",").map(Number).sort();
    const difficultyNames = difficultyIds.map((id) => API.getDifficultyNameById(id));
    return difficultyNames;
  },
  
  getWorkingDaysFromIdString: (workingDaysString) => {
    const weekDays = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"];
    const workingDaysArray = workingDaysString.replace(/{|}/g, "").split(",").map(Number).sort();
    return workingDaysArray.map(num => weekDays[num - 1]);
  },
  getDateFromTimeString: (timeString) => {
    const [hours, minutes] = timeString.split(':').map(Number);
    const date = new Date();
    date.setHours(hours, minutes);
    return date;
  }
}