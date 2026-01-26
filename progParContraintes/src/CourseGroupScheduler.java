import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.util.criteria.Criterion;

public class CourseGroupScheduler {
    public static void main(String[] args) {
        // Create a new model
        Model model = new Model("Course Scheduling");

        // Parameters
        int numCourses = 5;
        int numRooms = 3;
        int numTimeSlots = 4;
        int numTeachers = 4;
        int numGroups = 3;

        // Course requirements for each student group (1 means group must take the course)
        int[][] groupCourseRequirements = {
                {1, 1, 1, 0, 0},  // Group 0 must take courses 0, 1, and 2
                {0, 1, 1, 1, 0},  // Group 1 must take courses 1, 2, and 3
                {0, 0, 1, 1, 1}   // Group 2 must take courses 2, 3, and 4
        };

        // Decision Variables
        IntVar[] courseRooms = model.intVarArray("rooms", numCourses, 0, numRooms - 1);
        IntVar[] courseTimeSlots = model.intVarArray("timeSlots", numCourses, 0, numTimeSlots - 1);
        IntVar[] courseTeachers = model.intVarArray("teachers", numCourses, 0, numTeachers - 1);

        // Hard Constraints

        // 1. No two courses can be in the same room at the same time
        for (int i = 0; i < numCourses; i++) {
            for (int j = i + 1; j < numCourses; j++) {
                model.or(
                        model.arithm(courseTimeSlots[i], "!=", courseTimeSlots[j]),
                        model.arithm(courseRooms[i], "!=", courseRooms[j])
                ).post();
            }
        }

        // 2. A teacher can't teach two courses at the same time
        for (int i = 0; i < numCourses; i++) {
            for (int j = i + 1; j < numCourses; j++) {
                model.or(
                        model.arithm(courseTimeSlots[i], "!=", courseTimeSlots[j]),
                        model.arithm(courseTeachers[i], "!=", courseTeachers[j])
                ).post();
            }
        }

        // 3. Student group constraints - no overlapping courses for each group
        for (int g = 0; g < numGroups; g++) {
            for (int i = 0; i < numCourses; i++) {
                for (int j = i + 1; j < numCourses; j++) {
                    if (groupCourseRequirements[g][i] == 1 && groupCourseRequirements[g][j] == 1) {
                        // If both courses are required by this group, they cannot be at the same time
                        model.arithm(courseTimeSlots[i], "!=", courseTimeSlots[j]).post();
                    }
                }
            }
        }
        // 3. Specific teacher qualifications (example: teacher 0 can only teach courses 0 and 1)
        model.arithm(courseTeachers[0], "!=", 0).post();
        model.arithm(courseTeachers[2], "!=", 0).post();
        model.arithm(courseTeachers[3], "!=", 0).post();
        model.arithm(courseTeachers[4], "!=", 0).post();

        // 4. Room capacity constraints (example: room 2 can only host courses 3 and 4)
        model.arithm(courseRooms[0], "!=", 2).post();
        model.arithm(courseRooms[1], "!=", 2).post();
        model.arithm(courseRooms[2], "!=", 2).post();


        // Count violations for optimization
        IntVar violations = model.intVar("violations", 0, numCourses * numGroups);

        // Simple objective: minimize the number of teachers used
        IntVar maxTeacher = model.intVar("maxTeacher", 0, numTeachers - 1);
        model.max(maxTeacher, courseTeachers).post();

        // Set optimization objective
        model.setObjective(Model.MINIMIZE, maxTeacher);

        // Find solution
        var solver = model.getSolver();
        solver.limitSolution(10);

        var solutions = model.getSolver().findAllSolutions();
        System.out.println("nb solutions:" + solutions.size());

        for (var solution:solutions) {
            System.out.println("Solution found!");
            System.out.println("Max teacher used: " + solution.getIntVal(maxTeacher));

            for (int i = 0; i < numCourses; i++) {
                System.out.printf("Course %d: Room %d, Time slot %d, Teacher %d%n",
                        i,
                        solution.getIntVal(courseRooms[i]),
                        solution.getIntVal(courseTimeSlots[i]),
                        solution.getIntVal(courseTeachers[i])
                );
            }

            // Print schedule by group
            for (int g = 0; g < numGroups; g++) {
                System.out.printf("\nGroup %d courses:%n", g);
                for (int c = 0; c < numCourses; c++) {
                    if (groupCourseRequirements[g][c] == 1) {
                        System.out.printf("Course %d at time %d%n",
                                c,
                                solution.getIntVal(courseTimeSlots[c]));
                    }
                }
            }
        }
    }
}