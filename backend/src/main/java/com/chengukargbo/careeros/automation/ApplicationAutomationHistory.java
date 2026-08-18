package com.chengukargbo.careeros.automation;
import java.time.OffsetDateTime; import com.chengukargbo.careeros.automation.AutomationEnums.*; import jakarta.persistence.*;
@Entity @Table(name="application_automation_history")
public class ApplicationAutomationHistory { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) private ApplicationAutomation applicationAutomation; private Long applicationId;
 @Enumerated(EnumType.STRING) private State previousState; @Enumerated(EnumType.STRING) private State newState;
 @Enumerated(EnumType.STRING) private Source source; private OffsetDateTime occurredAt; private String reason;
 protected ApplicationAutomationHistory(){} ApplicationAutomationHistory(ApplicationAutomation automation,State previous,State next,Source source,String reason){applicationAutomation=automation;applicationId=automation.getApplication().getId();previousState=previous;newState=next;this.source=source;this.reason=reason;occurredAt=OffsetDateTime.now();}
 public Long getId(){return id;} public Long getApplicationId(){return applicationId;} public State getPreviousState(){return previousState;} public State getNewState(){return newState;} public Source getSource(){return source;} public OffsetDateTime getOccurredAt(){return occurredAt;} public String getReason(){return reason;}
}
