package functionalities;

import exception.DuplicateAppointmentException;
import exception.SniffException;
import functionalities.appointments.Appointment;
import functionalities.appointments.Consultation;
import functionalities.appointments.Surgery;
import functionalities.appointments.Vaccination;
import functionalities.ui.Ui;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class SniffTasks {

    private static final ArrayList<Appointment> APPOINTMENTS = new ArrayList<>();
    private static final HashSet<String> UIDS = new HashSet<>();

    public static void addAppointmentUID(String uid) {
        UIDS.add(uid);
    }

    public static void addAppointment(Appointment appointment) {
        APPOINTMENTS.add(appointment);
    }

    public static void retrieveTask(FileWriter savedFile) throws IOException {
        for (Appointment appointment : SniffTasks.APPOINTMENTS) {
            if (appointment.getStatus().equals(" ")) {
                savedFile.write(appointment.retrieveStorageInfo());
                savedFile.write(System.getProperty("line.separator"));
            }
        }
    }

    /**
     * Stores the data of archived tasks into SniffArchive file
     *
     * @param archiveSavedFile The SniffArchive file
     * @throws SniffException if there are errors when storing appointment data
     */
    public static void archivedTasks(FileWriter archiveSavedFile) throws SniffException {
        int no = 1;
        try {
            for (Appointment appointment : APPOINTMENTS) {
                if (appointment.isDone) {
                    archiveSavedFile.write(appointment.retrieveStorageInfo());
                    archiveSavedFile.write(System.getProperty("line.separator"));
                }
                no++;
            }
        } catch (IOException e) {
            throw new SniffException(" Error encountered storing appointment no" + no + " from task list!");
        }
    }

    /**
     * Sorts the ArrayList by date and time before listing out all the appointment currently in the appointment list.
     */
    public static void listArchivedAppointments() {
        APPOINTMENTS.sort(new DateTimeComparator());
        int counter = 1;
        int marked = 0;
        if (APPOINTMENTS.isEmpty()) {
            assert false : "There are no appointments in the task list";
            Ui.showUserMessage(" No entries found!");
        }
        for (Appointment appointment : APPOINTMENTS) {
            assert appointment != null : "Appointment is not empty";
            if (appointment.isDone) {
                Ui.formatPrintList(counter, appointment.toString());
                counter++;
                marked++;
            }
        }
        if (marked == 0) {
            assert false : "There are no archived appointments in the task list";
            Ui.showUserMessage(" No entries found!");
        }
    }

    /**
     * Sorts the ArrayList by date and time before listing out all the appointment currently in the appointment list.
     */
    public void listAppointments() {
        APPOINTMENTS.sort(new DateTimeComparator());
        int counter = 1;
        int notMarked = 0;
        if (APPOINTMENTS.isEmpty()) {
            assert false : "There are no appointments in the task list";
            Ui.showUserMessage(" No entries found!");
        }
        for (Appointment appointment : APPOINTMENTS) {
            assert appointment != null : "Appointment is not empty";
            if (appointment.getStatus().equals(" ")) {
                Ui.formatPrintList(counter, appointment.toString());
                counter++;
                notMarked++;
            }
        }
        if (notMarked == 0) {
            assert false : "There are no unmarked appointments in the task list";
            Ui.showUserMessage(" No entries found!");
        }
    }

    public void addConsultation(Animal animal, Owner owner,
                                LocalDate date, LocalTime time) throws SniffException {
        try {
            checkConsultationDuplicate(animal, owner, date, time);
            String uid = Uid.uidGenerator("C");
            while (UIDS.contains(uid)) { // this loop checks for duplicate appointment ids
                uid = Uid.uidGenerator("C");
            }
            UIDS.add(uid);
            Appointment newAppointment = new Consultation(uid, animal, owner, date, time);
            assert Objects.equals(newAppointment.uid, uid) : "consultation uid should be " + uid;
            assert !newAppointment.isDone : "consultation isDone should be false when initialized.";
            assert Objects.equals(newAppointment.animal.type, animal.type) :
                    "consultation animal type should be " + animal.type;
            assert Objects.equals(newAppointment.animal.name, animal.name) :
                    "consultation animal name should be " + animal.name;
            APPOINTMENTS.add(newAppointment);
            Ui.printAppointmentAddedMessage(newAppointment);
            Ui.showUserMessage(" Consultation added successfully!");
        } catch (StringIndexOutOfBoundsException e) {
            throw new SniffException(" Invalid consultation description!");
        } catch (DuplicateAppointmentException e) {
            System.out.println(" Appointment failed to be added.");
        }
    }

    /**
     * Checks if there is a duplicate consultation appointment in the ArrayList, throws an error if present.
     *
     * @param animal Animal object generated from input from user.
     * @param owner  Owner object generated from input from user.
     * @param date   LocalDate object generated from the date input from user.
     * @param time   LocalTime object generated from the time input from user.
     * @throws DuplicateAppointmentException thrown when the appointment details of another Consultation appointment
     *                                       currently in the ArrayList is same as the user input.
     */
    private void checkConsultationDuplicate(Animal animal, Owner owner,
                                            LocalDate date, LocalTime time) throws DuplicateAppointmentException {
        for (Appointment appointment : APPOINTMENTS) {
            if (appointment.uid.charAt(0) == 'C') {
                assert appointment.uid.charAt(0) != 'S' : "Surgery appointments should not reach here";
                assert appointment.uid.charAt(0) != 'V' : "Vaccination appointments should not reach here";
                Consultation temp = (Consultation) appointment;
                if (animal.equals(temp.animal) && owner.equals(temp.getOwner()) && date.equals(temp.getDate())
                        && time.equals(temp.getTime())) { //duplicate check
                    throw new DuplicateAppointmentException(" A similar appointment already exists.", temp);
                }
            }
        }
    }

    public void addVaccination(Animal animal, Owner owner,
                               LocalDate date, LocalTime time, String vaccine) throws SniffException {
        try {
            checkVaccinationDuplicate(animal, owner, date, time, vaccine);
            String uid = Uid.uidGenerator("V");
            while (UIDS.contains(uid)) { // this loop checks for duplicate appointment ids
                uid = Uid.uidGenerator("C");
            }
            UIDS.add(uid);
            Appointment newAppointment = new Vaccination(uid, animal, owner, date, time, vaccine);
            assert Objects.equals(newAppointment.uid, uid) : "vaccination uid should be " + uid;
            assert !newAppointment.isDone : "vaccination isDone should be false when initialized.";
            assert Objects.equals(newAppointment.animal.type, animal.type) :
                    "vaccination animal type should be " + animal.type;
            assert Objects.equals(newAppointment.animal.name, animal.name) :
                    "vaccination animal name should be " + animal.name;
            APPOINTMENTS.add(newAppointment);
            Ui.printAppointmentAddedMessage(newAppointment);
            Ui.showUserMessage(" Vaccination added successfully!");
        } catch (StringIndexOutOfBoundsException e) {
            throw new SniffException(" Invalid vaccination description!");
        } catch (DuplicateAppointmentException e) {
            System.out.println(" Appointment failed to be added.");
        }
    }

    /**
     * Checks if there is a duplicate vaccination appointment in the ArrayList, throws an error if present.
     *
     * @param animal  Animal object generated from input from user.
     * @param owner   Owner object generated from input from user.
     * @param date    LocalDate object generated from the date input from user.
     * @param time    LocalTime object generated from the time input from user.
     * @param vaccine String representing the vaccine description from the user.
     * @throws DuplicateAppointmentException thrown when the appointment details of another Vaccination appointment
     *                                       currently in the ArrayList is same as the user input.
     */
    private void checkVaccinationDuplicate(Animal animal, Owner owner, LocalDate date, LocalTime time,
                                           String vaccine) throws DuplicateAppointmentException {
        for (Appointment appointment : APPOINTMENTS) {
            if (appointment.uid.charAt(0) == 'V') {
                assert appointment.uid.charAt(0) != 'S' : "Surgery appointments should not reach here";
                assert appointment.uid.charAt(0) != 'C' : "Consultation appointments should not reach here";
                Vaccination temp = (Vaccination) appointment;
                if (animal.equals(temp.animal) && owner.equals(temp.getOwner()) && date.equals(temp.getDate())
                        && time.equals(temp.getTime()) && vaccine.equals(temp.getVaccine())) { //duplicate check
                    throw new DuplicateAppointmentException(" A similar appointment already exists.", temp);
                }
            }
        }
    }

    public void addSurgery(Animal animal, Owner owner,
                           String priority, LocalDate startDate, LocalTime startTime,
                           LocalDate endDate, LocalTime endTime) throws SniffException {
        try {
            checkSurgeryDuplicate(animal, owner, startDate, startTime, endDate, endTime);
            String uid = Uid.uidGenerator("S");
            while (UIDS.contains(uid)) { // this loop checks for duplicate appointment ids
                uid = Uid.uidGenerator("C");
            }
            UIDS.add(uid);
            Appointment newAppointment = new Surgery(uid, animal, owner, priority, startDate, startTime, endDate,
                    endTime);
            assert Objects.equals(newAppointment.uid, uid) : "surgery uid should be " + uid;
            assert !newAppointment.isDone : "surgery isDone should be false when initialized.";
            assert Objects.equals(newAppointment.animal.type, animal.type) :
                    "surgery animal type should be " + animal.type;
            assert Objects.equals(newAppointment.animal.name, animal.name) :
                    "surgery animal name should be " + animal.name;
            APPOINTMENTS.add(newAppointment);
            Ui.printAppointmentAddedMessage(newAppointment);
            Ui.showUserMessage(" Surgery added successfully!");
        } catch (StringIndexOutOfBoundsException e) {
            throw new SniffException(" Invalid surgery description!");
        } catch (DuplicateAppointmentException e) {
            System.out.println(" Appointment failed to be added.");
        }
    }

    /**
     * Checks if there is a duplicate surgical appointment in the ArrayList, throws an error if present.
     *
     * @param animal    Animal object generated from input from user.
     * @param owner     Owner object generated from input from user.
     * @param startDate LocalDate object generated from the start date input from user.
     * @param startTime LocalTime object generated from the start time input from user.
     * @param endDate   LocalDate object generated from the end date input from user.
     * @param endTime   LocalTime object generated from the end time input from user.
     * @throws DuplicateAppointmentException thrown when the appointment details of another Surgery appointment
     *                                       currently in the ArrayList is same as the user input.
     */
    private void checkSurgeryDuplicate(Animal animal, Owner owner, LocalDate startDate, LocalTime startTime,
                                       LocalDate endDate, LocalTime endTime) throws DuplicateAppointmentException {
        for (Appointment appointment : APPOINTMENTS) {
            if (appointment.uid.charAt(0) == 'S') {
                assert appointment.uid.charAt(0) != 'C' : "Consultation appointments should not reach here";
                assert appointment.uid.charAt(0) != 'V' : "Vaccination appointments should not reach here";
                Surgery temp = (Surgery) appointment;
                if (animal.equals(temp.animal) && owner.equals(temp.getOwner())
                        && startDate.equals(temp.getStartDate()) && startTime.equals(temp.getStartTime())
                        && endDate.equals(temp.getEndDate()) && endTime.equals(temp.getEndTime())) { //duplicate check
                    throw new DuplicateAppointmentException(" A similar appointment already exists.", temp);
                }
            }
        }
    }

    public void removeAppointment(String uid) throws SniffException {
        try {
            if (!UIDS.contains(uid)) {
                throw new SniffException(" There are no appointments with this ID.");
            }
            int index = 0;
            for (int i = 0; i < APPOINTMENTS.size(); i++) {
                if (uid.equals(APPOINTMENTS.get(i).uid)) {
                    index = i;
                    break;
                }
            }
            Appointment temp = APPOINTMENTS.get(index);
            APPOINTMENTS.remove(index);
            UIDS.remove(uid);
            Ui.printAppointmentRemovedMessage(temp);
        } catch (IndexOutOfBoundsException e) {
            throw new SniffException(" The remove command entry is invalid!");
        }
    }

    /**
     * Searches through APPOINTMENTS arraylist and checks if appointment with matching uID is stored
     * Prints out appointment details if found
     *
     * @param uId the appointment ID to find in stored appointments
     * @throws SniffException if no appointment ID is given by user
     */
    public void findAppointment(String uId) throws SniffException {
        int counter = 1;
        for (Appointment appointment : APPOINTMENTS) {
            assert appointment.uid != null;
            if (uId.equalsIgnoreCase(appointment.uid)) {
                Ui.formatPrintList(counter, appointment.toString());
                counter++;
            }
        }
        if (uId.equals("")) {
            throw new SniffException(" No appointment ID provided!");
        }
        if (counter == 1) {
            Ui.showUserMessage(" There are no appointments with this ID!");
        }
    }

    /**
     * Searches through APPOINTMENTS arraylist and checks if appointment with matching uID is stored
     * Prints out all appointment details for matching animal if found
     *
     * @param animal the animal type to find in stored appointments
     */
    public void findAnimal(String animal) {
        int counter = 1;
        for (Appointment appointment : APPOINTMENTS) {
            assert appointment.animal.type != null;
            if (animal.equalsIgnoreCase(appointment.animal.type)) {
                Ui.formatPrintList(counter, appointment.toString());
                counter++;
            }
        }
        if (counter == 1) {
            Ui.showUserMessage(" There are no appointments for this animal type!");
        }
    }

    /**
     * Searches through APPOINTMENTS arraylist and checks for given appointment type (surgery, consultation etc.)
     * Prints out all appointment details for matching appointment types if found
     *
     * @param type the appointment type to find
     */
    public void findType(String type) {
        int counter = 1;
        for (Appointment appointment : APPOINTMENTS) {
            assert appointment != null;
            if (type.equalsIgnoreCase(appointment.getDescription())) {
                Ui.formatPrintList(counter, appointment.toString());
                counter++;
            }
        }
        if (counter == 1) {
            Ui.showUserMessage(" There are no appointments of this type!");
        }
    }

    /**
     * Searches through APPOINTMENTS arraylist and checks for appointments on given date
     * Prints out all appointment details for given date if found
     *
     * @param date the date to check for existing appointments
     * @throws SniffException if invalid format is given for date
     */
    public void findDate(String date) throws SniffException {
        int counter = 1;
        for (Appointment appointment : APPOINTMENTS) {
            if (appointment.isDate(date)) {
                Ui.formatPrintList(counter, appointment.toString());
                counter++;
            }
        }
        if (counter == 1) {
            Ui.showUserMessage(" There are no appointments on this date!");
        }
    }

    //@@author 11-Navya
    public void markAppointment(String uid) throws SniffException {
        try {
            if (!UIDS.contains(uid)) {
                throw new SniffException(" There are no appointments with this ID.");
            }
            int index = 0;
            for (int i = 0; i < APPOINTMENTS.size(); i++) {
                if (uid.equals(APPOINTMENTS.get(i).uid)) {
                    index = i;
                    break;
                }
            }
            if (APPOINTMENTS.get(index).getStatus().equals("X")) {
                Ui.printAppointmentAlreadyMarkedMessage();
            } else {
                APPOINTMENTS.get(index).setIsDone(true);
                Ui.printAppointmentMarkMessage();
            }
        } catch (IndexOutOfBoundsException e) {
            throw new SniffException(" The mark command entry is invalid!");
        }

    }

    public void unMarkAppointment(String uid) throws SniffException {
        try {
            if (!UIDS.contains(uid)) {
                throw new SniffException(" There are no appointments with this ID.");
            }
            int index = 0;
            for (int i = 0; i < APPOINTMENTS.size(); i++) {
                if (uid.equals(APPOINTMENTS.get(i).uid)) {
                    index = i;
                    break;
                }
            }
            if (APPOINTMENTS.get(index).getStatus().equals(" ")) {
                Ui.printAppointmentAlreadyUnMarkedMessage();
            } else {
                APPOINTMENTS.get(index).setIsDone(false);
                Ui.printAppointmentUnMarkMessage();
            }
        } catch (IndexOutOfBoundsException e) {
            throw new SniffException(" The unMark command entry is invalid!");
        }
    }
}
