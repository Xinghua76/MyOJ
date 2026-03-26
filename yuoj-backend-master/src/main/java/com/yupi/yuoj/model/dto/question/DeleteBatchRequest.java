package com.yupi.yuoj.model.dto.question;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * Delete batch request
 */
@Data
public class DeleteBatchRequest implements Serializable {

    /**
     * ids to delete
     */
    private List<Long> ids;

    private static final long serialVersionUID = 1L;
}
