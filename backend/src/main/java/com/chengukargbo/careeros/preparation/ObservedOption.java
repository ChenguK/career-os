package com.chengukargbo.careeros.preparation;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "observed_options")
public class ObservedOption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "observed_question_id", nullable = false)
    private ObservedQuestion question;
    @Column(name = "external_option_id", length = 200)
    private String externalOptionId;
    @Column(name = "option_value", nullable = false, length = 1000)
    private String optionValue;
    @Column(name = "option_label", nullable = false, length = 1000)
    private String optionLabel;
    @Column(name = "display_order", nullable = false) private int displayOrder;
    @Column(nullable = false) private boolean active;

    protected ObservedOption() {}

    ObservedOption(ObservedQuestion question, String externalOptionId,
        String value, String label, int displayOrder, boolean active) {
        this.question = question;
        this.externalOptionId = externalOptionId;
        optionValue = value;
        optionLabel = label;
        this.displayOrder = displayOrder;
        this.active = active;
    }

    public Long getId() { return id; }
    public String getExternalOptionId() { return externalOptionId; }
    public String getOptionValue() { return optionValue; }
    public String getOptionLabel() { return optionLabel; }
    public int getDisplayOrder() { return displayOrder; }
    public boolean isActive() { return active; }
}
