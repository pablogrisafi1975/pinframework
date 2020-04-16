package com.pinframework;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class UserService {
    private final Map<Long, UserDTO> USER_MAP = createMap();

    private Map<Long, UserDTO> createMap() {
        return LongStream.range(0, 10)
                .mapToObj(l -> new UserDTO(l, "firstName" + l, "lastName" + l))
                .collect(Collectors.toMap(u -> u.getId(), u -> u));
    }

    public void reset() {
        USER_MAP.clear();
        USER_MAP.putAll(createMap());
    }

    /**
     * @param id
     * @return the user with said id or null. If id < 0 throws an NullPointerException
     */
    public UserDTO get(Long id) {
        if (id < 0) {
            throw new NullPointerException("Fake internal error");
        }
        return USER_MAP.get(id);
    }

    /**
     * @param user
     * @return the saved user new value
     */
    public UserDTO update(UserDTO user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("User " + user + " has no id");
        }
        if (!USER_MAP.containsKey(user.getId())) {
            throw new IllegalArgumentException("User " + user + " does not exist");
        }
        USER_MAP.put(user.getId(), user);

        return user;
    }

    /**
     * @param user
     * @return
     */
    public UserDTO savNew(UserDTO user) {
        if (user.getId() != null) {
            throw new IllegalArgumentException("User " + user + " has no id");
        }
        if (USER_MAP.containsKey(user.getId())) {
            throw new IllegalArgumentException("User " + user + " already exist");
        }

        user.setId(USER_MAP.keySet().stream().max(Comparator.naturalOrder()).orElse(0L) + 1L);

        USER_MAP.put(user.getId(), user);

        return user;
    }

    public UserDTO delete(Long id) {
        if (!USER_MAP.containsKey(id)) {
            throw new IllegalArgumentException("User with id " + id + " does not exist");
        }

        return USER_MAP.remove(id);
    }

    public List<UserDTO> list() {
        return USER_MAP.values().stream().sorted(Comparator.comparing(UserDTO::getId)).collect(Collectors.toList());
    }

    public List<UserDTO> list(String expectedFirstName, String expectedLastName) {
        Predicate<UserDTO> firstNameFilter = expectedFirstName == null ? (UserDTO u) -> true : (UserDTO u) -> u.getFirstName().toLowerCase(
                Locale.ENGLISH).contains(expectedFirstName.toLowerCase(Locale.ENGLISH));

        Predicate<UserDTO> lastNameFilter = expectedLastName == null ? (UserDTO u) -> true : (UserDTO u) -> u.getLastName().toLowerCase(
                Locale.ENGLISH).contains(expectedLastName.toLowerCase(Locale.ENGLISH));

        return USER_MAP.values().stream().
                filter(firstNameFilter).
                filter(lastNameFilter).
                sorted(Comparator.comparing(UserDTO::getId)).collect(Collectors.toList());
    }

}
