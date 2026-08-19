package com.chengukargbo.careeros.preparation;

import java.util.*;

import com.chengukargbo.careeros.questions.QuestionEnums.AnswerType;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "observed_questions")
public class ObservedQuestion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "snapshot_id", nullable = false)
    private FormObservationSnapshot snapshot;
    @Column(name = "external_question_id", nullable = false, length = 200)
    private String externalQuestionId;
    @Column(name = "question_text", nullable = false, length = 1000)
    private String questionText;
    @Enumerated(EnumType.STRING) @Column(name = "answer_type", nullable = false, length = 30)
    private AnswerType answerType;
    @Column(nullable = false) private boolean required;
    @Column(name = "page_key", nullable = false, length = 200) private String pageKey;
    @Column(nullable = false) private boolean active;
    @Column(name = "display_order", nullable = false) private int displayOrder;
    @Column(name = "question_fingerprint", nullable = false, length = 64)
    private String questionFingerprint;
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC, optionValue ASC")
    private List<ObservedOption> options = new ArrayList<>();

    protected ObservedQuestion() {}

    ObservedQuestion(FormObservationSnapshot snapshot, String externalQuestionId,
        String questionText, AnswerType answerType, boolean required,
        boolean active, int displayOrder, String fingerprint) {
        this(snapshot, externalQuestionId, questionText, answerType, required,
            "application", active, displayOrder, fingerprint);
    }

    ObservedQuestion(FormObservationSnapshot snapshot, String externalQuestionId,
        String questionText, AnswerType answerType, boolean required, String pageKey,
        boolean active, int displayOrder, String fingerprint) {
        this.snapshot = snapshot;
        this.externalQuestionId = externalQuestionId;
        this.questionText = questionText;
        this.answerType = answerType;
        this.required = required;
        this.pageKey = pageKey;
        this.active = active;
        this.displayOrder = displayOrder;
        questionFingerprint = fingerprint;
    }

    void add(ObservedOption option) { options.add(option); }

    public Long getId() { return id; }
    public FormObservationSnapshot getSnapshot() { return snapshot; }
    public String getExternalQuestionId() { return externalQuestionId; }
    public String getQuestionText() { return questionText; }
    public AnswerType getAnswerType() { return answerType; }
    public boolean isRequired() { return required; }
    public String getPageKey() { return pageKey; }
    public boolean isActive() { return active; }
    public int getDisplayOrder() { return displayOrder; }
    public String getQuestionFingerprint() { return questionFingerprint; }
    public List<ObservedOption> getOptions() { return List.copyOf(options); }
}
