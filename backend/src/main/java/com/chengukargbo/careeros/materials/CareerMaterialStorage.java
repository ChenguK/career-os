package com.chengukargbo.careeros.materials;

import java.io.IOException;
import java.io.InputStream;

public interface CareerMaterialStorage {
    String store(InputStream content) throws IOException;
    StoredMaterial read(String storageKey) throws IOException;
    void delete(String storageKey) throws IOException;
    record StoredMaterial(InputStream content, long size) implements AutoCloseable {
        @Override public void close() throws IOException { content.close(); }
    }
}
