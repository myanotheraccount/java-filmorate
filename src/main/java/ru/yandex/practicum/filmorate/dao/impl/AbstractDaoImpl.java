package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AbstractDaoImpl {
    protected String readSql(final String filename) {
        Resource resource = new ClassPathResource("sql/" + filename + ".sql");
        try {
            InputStream inputStream = resource.getInputStream();
            return new String(FileCopyUtils.copyToByteArray(inputStream),
                    StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new NotFoundException("запрос не найден");
        }
    }
}
