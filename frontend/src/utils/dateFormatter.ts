export const formatDateFromArray = (dateArray: number[]) :string => {
    if (!dateArray || dateArray.length < 3) {
        return 'Некорректная дата';
    }
    const year = dateArray[0];
    const month = String(dateArray[1]).padStart(2, '0');
    const day = String(dateArray[2]).padStart(2, '0');

    return `${day}.${month}.${year}`;
}