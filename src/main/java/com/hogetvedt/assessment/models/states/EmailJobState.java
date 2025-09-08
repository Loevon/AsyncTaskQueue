package com.hogetvedt.assessment.models.states;

import java.util.UUID;

public record EmailJobState(UUID emailId) implements JobState {}
