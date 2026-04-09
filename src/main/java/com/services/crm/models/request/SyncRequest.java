package com.services.crm.models.request;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SyncRequest {
    private String tenantId;
    private List<Map<String, Object>> records;
}