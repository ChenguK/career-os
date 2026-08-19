package com.chengukargbo.careeros.materials;

import java.io.*;
import java.nio.file.*;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalCareerMaterialStorage implements CareerMaterialStorage {
    private final Path root;
    public LocalCareerMaterialStorage(@Value("${careeros.materials.storage-root:${user.home}/.careeros/materials}") String root) {
        this.root=Path.of(root).toAbsolutePath().normalize();
    }
    @Override public String store(InputStream content) throws IOException {
        Files.createDirectories(root);
        String key=UUID.randomUUID().toString();
        Path target=resolve(key); Path temporary=Files.createTempFile(root,"upload-",".tmp");
        try { Files.copy(content,temporary,StandardCopyOption.REPLACE_EXISTING);
            Files.move(temporary,target,StandardCopyOption.ATOMIC_MOVE); return key;
        } finally { Files.deleteIfExists(temporary); }
    }
    @Override public StoredMaterial read(String key) throws IOException {
        Path path=resolve(key); return new StoredMaterial(Files.newInputStream(path),Files.size(path));
    }
    @Override public void delete(String key) throws IOException { Files.deleteIfExists(resolve(key)); }
    private Path resolve(String key) {
        if(key==null || !key.matches("^[0-9a-fA-F-]{36}$")) throw new IllegalArgumentException("Invalid material storage key");
        Path result=root.resolve(key).normalize();
        if(!result.getParent().equals(root)) throw new IllegalArgumentException("Invalid material storage key");
        return result;
    }
}
