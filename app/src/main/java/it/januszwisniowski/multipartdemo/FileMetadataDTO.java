package it.januszwisniowski.multipartdemo;

import java.util.List;

public record FileMetadataDTO(String name, List<String> tags) {
    public FileMetadataDTO(String name, List<String> tags) {
        this.name = name;
        this.tags = List.copyOf(tags);
    }
}
