package com.services.crm.models.response;

import java.util.List;

public record DashboardResponse(
    boolean hasOpenCashSession,
    CashSessionDTO cashSession,
    CashSessionDTO lastClosedSession,
    List<String> messages
) {}
