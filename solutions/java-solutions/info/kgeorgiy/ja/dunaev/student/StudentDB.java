package info.kgeorgiy.ja.dunaev.student;

import info.kgeorgiy.java.advanced.student.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements AdvancedQuery {
    private static final Comparator<Student> STUDENT_COMPARATOR = Comparator.comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
            .thenComparing(Comparator.comparingInt(Student::getId).reversed());

    private static final Comparator<Group> GROUP_BY_STUDENT_SIZE_COMPARATOR =
            Comparator.comparing(Group::getStudents, Comparator.comparingInt(List::size))
                    .thenComparing(Group::getName);

    private static final Comparator<Group> GROUP_BY_STUDENT_DISTINCT_NAMES_COMPARATOR =
            Comparator.comparing(Group::getStudents, Comparator.comparingLong(StudentDB::getDistinctNamesCount))
                    .thenComparing(Comparator.comparing(Group::getName).reversed());
    public static final Comparator<Map.Entry<String, Integer>> COMPARE_OCCURRENCES_DESC_THAN_NAME =
            Map.Entry.<String, Integer>comparingByValue()
                    .thenComparing(Map.Entry.<String, Integer>comparingByKey().reversed());

    public static final Comparator<Map.Entry<String, Integer>> COMPARE_OCCURRENCES_ASC_THAN_NAME =
            Map.Entry.<String, Integer>comparingByValue().reversed()
                    .thenComparing(Map.Entry.<String, Integer>comparingByKey().reversed());

    private static String getFullName(Student student) {
        return student.getFirstName() + " " + student.getLastName();
    }

    private static long getDistinctNamesCount(List<Student> students) {
        return students.stream()
                .map(Student::getFirstName)
                .distinct()
                .count();
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getStudentCharacteristic(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getStudentCharacteristic(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return getStudentCharacteristic(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getStudentCharacteristic(students, StudentDB::getFullName);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return recollect(getFirstNames(students), Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return maxMapOrElse(students, Student::compareTo, Student::getFirstName, "");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortBy(students, Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortBy(students, STUDENT_COMPARATOR);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findBy(students, Student::getFirstName, name);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findBy(students, Student::getLastName, name);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findBy(students, Student::getGroup, group);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return recollect(
                findStudentsByGroup(students, group),
                Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        (s1, s2) -> Collections.min(List.of(s1, s2))));
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroupsSorted(students, STUDENT_COMPARATOR);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroupsSorted(students, Student::compareTo);
    }

    @Override
    public GroupName getLargestGroup(Collection<Student> students) {
        return getGroupBy(students, GROUP_BY_STUDENT_SIZE_COMPARATOR);
    }

    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> students) {
        return getGroupBy(students, GROUP_BY_STUDENT_DISTINCT_NAMES_COMPARATOR);
    }

    private <R> List<R> getStudentCharacteristic(Collection<Student> students, Function<Student, R> mapper) {
        return students.stream()
                .map(mapper)
                .toList();
    }

    private List<Student> streamSortBy(Stream<Student> studentStream, Comparator<Student> comp) {
        return studentStream.sorted(comp).toList();
    }

    private List<Student> sortBy(Collection<Student> students, Comparator<Student> comp) {
        return streamSortBy(students.stream(), comp);
    }

    private <T> List<Student> findBy(Collection<Student> students, Function<Student, T> keyExtractor, T objToCompare) {
        return streamSortBy(students.stream()
                        .filter(s -> keyExtractor.apply(s).equals(objToCompare)),
                STUDENT_COMPARATOR);
    }

    private Map<GroupName, List<Student>> mapGroupSortedStudents(Collection<Student> students, Comparator<Student> comp) {
        return recollect(sortBy(students, comp), Collectors.groupingBy(Student::getGroup));
    }

    private List<Group> getGroupsSorted(Collection<Student> students, Comparator<Student> comp) {
        return mapGroupSortedStudents(students, comp).entrySet().stream()
                .map(entry -> new Group(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(Group::getName))
                .toList();
    }

    private GroupName getGroupBy(Collection<Student> students, Comparator<Group> comp) {
        return maxMapOrElse(getGroupsByName(students), comp, Group::getName, null);
    }

    private <T, R> R recollect(Collection<T> students, Collector<T, ?, R> downstream) {
        return students.stream()
                .collect(downstream);
    }

    private <T, R> R maxMapOrElse(Collection<T> students, Comparator<T> maxBy, Function<T, R> mapper, R defaultValue) {
        return students.stream()
                .max(maxBy)
                .map(mapper)
                .orElse(defaultValue);
    }

    @Override
    public String getMostPopularName(Collection<Student> students) {
        return getNameByOccurrences(students, COMPARE_OCCURRENCES_DESC_THAN_NAME);
    }

    @Override
    public String getLeastPopularName(Collection<Student> students) {
        return getNameByOccurrences(students, COMPARE_OCCURRENCES_ASC_THAN_NAME);
    }

    private String getNameByOccurrences(Collection<Student> students, Comparator<Map.Entry<String, Integer>> maxBy) {
        return maxMapOrElse(
                countStudentNameOccurrences(students).entrySet(),
                maxBy,
                Map.Entry::getKey,
                ""
        );
    }

    private Map<String, Integer> countStudentNameOccurrences(Collection<Student> students) {
        return recollect(students, Collectors.groupingBy(Student::getFirstName,
                Collectors.collectingAndThen(
                        Collectors.mapping(Student::getGroup, Collectors.toSet()),
                        Set::size)));
    }

    @Override
    public List<String> getFirstNames(Collection<Student> students, int[] indices) {
        return getByIds(students, indices, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(Collection<Student> students, int[] indices) {
        return getByIds(students, indices, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(Collection<Student> students, int[] indices) {
        return getByIds(students, indices, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(Collection<Student> students, int[] indices) {
        return getByIds(students, indices, StudentDB::getFullName);
    }

    private <R> List<R> getByIds(Collection<Student> students, int[] indices, Function<Student, R> mapper) {
        return Arrays.stream(indices)
                .mapToObj(recollect(students, Collectors.toList())::get)
                .map(mapper)
                .toList();
    }
}
