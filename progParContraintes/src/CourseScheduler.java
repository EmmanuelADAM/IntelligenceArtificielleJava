import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

/**
 * Course Scheduling Problem
 * Constrant Programming example using Choco Solver
 *
 * Four teachers (Durand, Dupont, Dupond, and Lagaffe) must teach in two different rooms (A, B) during four possible time slots (8:00-10:00, 10:00-12:00, 14:00-16:00, and 16:00-18:00).
 * The subjects are Math, Physics, Chemistry, Biology, and History.
 *
 * Of course, in any given room, at any given time slot, there can only be one class and one teacher.
 * A teacher can only teach one class during a given time slot.
 * Durand can only teach Math or Physics.
 * Dupont can only teach Physics or Chemistry.
 * Dupond can only teach Chemistry or Biology.
 * Lagaffe can only teach Biology or History.
 * Physics and Chemistry classes can only be held in room B due to equipment needs.
 * This room B in fact can only host Physics and Chemistry classes.
 * The goal is to assign each course to a room, a time slot, and a teacher while satisfying all the constraints.
 *
 * */
public class CourseScheduler {
    public static void main(String[] args) {
        // Create a new model
        Model model = new Model("Course Scheduling");

        // Parameters
        enum Slots {_08h_10h, _10h_12h, _14h_16h, _16h_18h};
        int numTimeSlots = Slots.values().length;

        enum Courses {Math, Physics, Chemistry, Biology, History};
        int numCourses = Courses.values().length;

        enum Rooms {A, B};
        int numRooms = Rooms.values().length;

        enum Profs {Durand, Dupont, Dupond, Lagaffe};
        int numTeachers = Profs.values().length;

        // Decision Variables
        // For each course, we need to assign a room, time slot, and teacher
        IntVar[] courseRooms = model.intVarArray("rooms", numCourses, 0, numRooms - 1);
        //courseRooms[0] = room assigned to course 0 (Math), etc.
        IntVar[] courseTimeSlots = model.intVarArray("slots", numCourses, 0, numTimeSlots - 1);
        //courseTimeSlots[0] = time slot assigned to course 0 (Math), etc.
        IntVar[] courseTeachers = model.intVarArray("profs", numCourses, 0, numTeachers - 1);
        //courseTeachers[0] = prof assigned to course 0 (Math), etc.

        // Constraints
        // 1. No two courses can be in the same room at the same time (slot)
        for (int i = 0; i < numCourses; i++) {
            for (int j = i + 1; j < numCourses; j++) {
                // If courses i and j are at the same time slot, they must be in different rooms
                // Equivalent to: not (same time slot and same room)
                // not ( (courseTimeSlots[i] = courseTimeSlots[j]) and (courseRooms[i] = courseRooms[j]) )
                // which is the same as, because of De Morgan's laws not (A and B) <=> not A or not B
                // not courseTimeSlots[i] = courseTimeSlots[j]  or  not courseRooms[i] != courseRooms[j]
                model.or(model.arithm(courseTimeSlots[i], "!=", courseTimeSlots[j]),
                         model.arithm(courseRooms[i], "!=", courseRooms[j])).post();
            }
        }

        // 2. A teacher can't teach two courses at the same time
        for (int i = 0; i < numCourses-1; i++) {
            for (int j = i + 1; j < numCourses; j++) {
                model.or(
                        model.arithm(courseTimeSlots[i], "!=", courseTimeSlots[j]),
                        model.arithm(courseTeachers[i], "!=", courseTeachers[j])

                ).post();
            }
        }

        // 3. Specific teacher qualifications
        //  Durand can only teach Math and Physics
        //  Dupont can only teach Chemistry and Physics
        //  Dupond can only teach Chemistry and Biology
        //  Lagaffe can only teach Biology and History

        // so Chemistry, Biology, History cannot be assigned to Durand
        model.arithm(courseTeachers[Courses.Chemistry.ordinal()], "!=", Profs.Durand.ordinal()).post();
        model.arithm(courseTeachers[Courses.Biology.ordinal()], "!=", Profs.Durand.ordinal()).post();
        model.arithm(courseTeachers[Courses.History.ordinal()], "!=", Profs.Durand.ordinal()).post();
        // so Math, Biology, History cannot be assigned to Dupont
        model.arithm(courseTeachers[Courses.Math.ordinal()], "!=", Profs.Dupont.ordinal()).post();
        model.arithm(courseTeachers[Courses.Biology.ordinal()], "!=", Profs.Dupont.ordinal()).post();
        model.arithm(courseTeachers[Courses.History.ordinal()], "!=", Profs.Dupont.ordinal()).post();
        // so Math, Physics, History cannot be assigned to Dupond
        model.arithm(courseTeachers[Courses.Math.ordinal()], "!=", Profs.Dupond.ordinal()).post();
        model.arithm(courseTeachers[Courses.Physics.ordinal()], "!=", Profs.Dupond.ordinal()).post();
        model.arithm(courseTeachers[Courses.History.ordinal()], "!=", Profs.Dupond.ordinal()).post();
        // so Math, Physics, Chemistry cannot be assigned to Lagaffe
        model.arithm(courseTeachers[Courses.Math.ordinal()], "!=", Profs.Lagaffe.ordinal()).post();
        model.arithm(courseTeachers[Courses.Physics.ordinal()], "!=", Profs.Lagaffe.ordinal()).post();
        model.arithm(courseTeachers[Courses.Chemistry.ordinal()], "!=", Profs.Lagaffe.ordinal()).post();


        // 4. Room capacity constraints (example: Physics and Chemistry courses can only be hosted in room B )
        // and Room B can only host Physics and Chemistry courses
        model.arithm(courseRooms[Courses.Physics.ordinal()], "=", Rooms.B.ordinal()).post();
        model.arithm(courseRooms[Courses.Chemistry.ordinal()], "=", Rooms.B.ordinal()).post();
        model.arithm(courseRooms[Courses.Math.ordinal()], "!=", Rooms.B.ordinal()).post();
        model.arithm(courseRooms[Courses.Biology.ordinal()], "!=", Rooms.B.ordinal()).post();
        model.arithm(courseRooms[Courses.History.ordinal()], "!=", Rooms.B.ordinal()).post();

        // Solve the model
        model.getSolver().printShortStatistics();

        // Find solution
        if (model.getSolver().solve()) {
            // Print solution
            System.out.println("Solution found!");
            System.out.println(model.getSolver().getSolutionCount() + " solution(s) found.");
            // Display all the solutions
            for (int i = 0; i < numCourses; i++) {
                System.out.printf("Course %s: Room %s, Time Slot %s, Teacher %s\n",
                        Courses.values()[i],
                        Rooms.values()[courseRooms[i].getValue()],
                        Slots.values()[courseTimeSlots[i].getValue()],
                        Profs.values()[courseTeachers[i].getValue()]);
            }


        }
        else {
            System.out.println("No solution found.");
        }
    }
}