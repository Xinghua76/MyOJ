package com.yupi.yuoj.model.dto.contest;

import java.io.Serializable;
import lombok.Data;

/**
 * Contest signup request
 */
@Data
public class ContestSignupRequest implements Serializable {

    private Long contestId;

    private static final long serialVersionUID = 1L;
}
