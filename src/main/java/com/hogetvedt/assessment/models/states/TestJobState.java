package com.hogetvedt.assessment.models.states;

import java.util.UUID;

public record TestJobState(UUID jobId) implements JobState {}
