package org.zgc.nio.server;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class OffsetPosition {
    long offset;
    int position;
}
