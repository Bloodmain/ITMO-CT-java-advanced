package info.kgeorgiy.java.advanced.student;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Tests for hard version
 * of <a href="https://www.kgeorgiy.info/courses/java-advanced/homeworks.html#homework-student">Student</a> homework
 * for <a href="https://www.kgeorgiy.info/courses/java-advanced/">Java Advanced</a> course.
 *
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class GroupQueryTest extends StudentQueryTest {
    private final GroupQuery db = createCUT();

    public GroupQueryTest() {
    }

    @Test
    public void test21_testGetGroupsByName() {
        testGroups(db::getGroupsByName, new int[][]{{0}, {0, 1}, {0, 2, 1}});
    }

    @Test
    public void test22_testGetGroupsById() {
        testGroups(db::getGroupsById, new int[][]{{0}, {0, 1}, {0, 2, 1}});
    }

    @Test
    public void test23_testGetLargestGroup() {
        testString(db::getLargestGroup, "M3238", "M3239", "M3238");
    }

    @Test
    public void test24_testGetLargestGroupByFirstName() {
        testString(db::getLargestGroupFirstName, "M3238", "M3238", "M3238");
    }

    private void testGroups(final Function<List<Student>, List<Group>> query, final int[]... answers) {
        test(query, GroupQueryTest::groups, answers);
    }

    public static List<Group> groups(final List<Student> students, final int[] answer) {
        GroupName group = null;
        List<Student> groupStudents = new ArrayList<>();
        final List<Group> groups = new ArrayList<>();

        for (final Student student : getStudents(students, answer)) {
            if (group != null && !group.equals(student.getGroup())) {
                groups.add(new Group(group, groupStudents));
                groupStudents = new ArrayList<>();
            }
            group = student.getGroup();
            groupStudents.add(student);
        }
        if (!groupStudents.isEmpty()) {
            groups.add(new Group(group, groupStudents));
        }
        return groups;
    }
}
