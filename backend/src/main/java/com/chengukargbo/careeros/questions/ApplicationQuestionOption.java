package com.chengukargbo.careeros.questions;

import jakarta.persistence.*;

@Entity
@Table(name = "application_question_options")
public class ApplicationQuestionOption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "application_question_id", nullable = false)
    private ApplicationQuestion question;
    @Column(name = "external_option_id", length = 200)
    private String externalOptionId;
    @Column(name = "option_value", nullable = false, length = 1000)
    private String value;
    @Column(name = "option_label", nullable = false, length = 1000)
    private String label;
    @Column(name = "display_order", nullable = false)
    private int displayOrder;
    @Column(nullable = false)
    private boolean active;

    protected ApplicationQuestionOption() {}

    ApplicationQuestionOption(ApplicationQuestion question, String externalOptionId,
        String value, String label, int displayOrder, boolean active) {
        this.question = question; this.externalOptionId = externalOptionId;
        this.value = value; this.label = label;
        this.displayOrder = displayOrder; this.active = active;
    }

    public Long getId() { return id; }
    public String getExternalOptionId() { return externalOptionId; }
    public String getValue() { return value; }
    public String getLabel() { return label; }
    public int getDisplayOrder() { return displayOrder; }
    public boolean isActive() { return active; }
}
