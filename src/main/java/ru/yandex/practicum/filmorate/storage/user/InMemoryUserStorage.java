package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Long, User> storage = new HashMap<>();

    @Override
    public Boolean isExist(Long id) {
        return storage.containsKey(id);
    }

    @Override
    public User update(User user) {
        storage.put(user.getId(), user);
        return user;
    }

    @Override
    public void remove(Long id) {
        storage.remove(id);
    }

    @Override
    public User get(Long id) {
        return storage.get(id);
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(storage.values());
    }
}
