package com.codemaniac.jobtrackrai.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BrightDataSnapshotRequest {

  private List<Input> input;

  @Data
  @AllArgsConstructor
  public static class Input {
    private String url;
  }
}
