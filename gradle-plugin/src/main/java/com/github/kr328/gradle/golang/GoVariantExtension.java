package com.github.kr328.gradle.golang;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode
@Data
@NoArgsConstructor
public class GoVariantExtension {
    private Set<String> buildTags = new HashSet<>();
}
