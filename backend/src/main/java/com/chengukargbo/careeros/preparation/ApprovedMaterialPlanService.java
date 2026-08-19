package com.chengukargbo.careeros.preparation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chengukargbo.careeros.applications.Application;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.materials.CareerMaterial;
import com.chengukargbo.careeros.preparation.ApprovedMaterialPlan.AuthoritySource;
import com.chengukargbo.careeros.profile.*;

@Service @Transactional
public class ApprovedMaterialPlanService {
    private final ApplicationPreparationSessionRepository sessions;private final ApprovedMaterialPlanRepository plans;private final ApplicantProfileRepository profiles;
    public ApprovedMaterialPlanService(ApplicationPreparationSessionRepository sessions,ApprovedMaterialPlanRepository plans,ApplicantProfileRepository profiles){this.sessions=sessions;this.plans=plans;this.profiles=profiles;}
    public Response create(Long applicationId,Long sessionId){ApplicationPreparationSession session=session(applicationId,sessionId);ApprovedMaterialPlan existing=plans.findBySessionId(sessionId).orElse(null);if(existing!=null)return response(existing);
        Selection selection=select(session.getApplication());if(selection==null)return new Response(null,sessionId,true,null,null,null,null,null,null);
        return response(plans.saveAndFlush(new ApprovedMaterialPlan(session,selection.material,selection.source)));}
    @Transactional(readOnly=true) public Response get(Long applicationId,Long sessionId){session(applicationId,sessionId);ApprovedMaterialPlan plan=plans.findBySessionId(sessionId).orElse(null);return plan==null?new Response(null,sessionId,true,null,null,null,null,null,null):response(plan);}
    private Selection select(Application app){if(app.getResumeMaterial()!=null)return new Selection(app.getResumeMaterial(),AuthoritySource.APPLICATION_SELECTION);ApplicantProfile profile=profiles.findByProfileKey(ApplicantProfile.PRIMARY_PROFILE_KEY).orElse(null);CareerMaterial fallback=profile==null?null:profile.getDefaultResumeMaterial();return fallback!=null&&fallback.isActive()?new Selection(fallback,AuthoritySource.PROFILE_DEFAULT):null;}
    private ApplicationPreparationSession session(Long app,Long session){return sessions.findByIdAndApplicationId(session,app).orElseThrow(()->new BusinessValidationException("Preparation session not found for application"));}
    private Response response(ApprovedMaterialPlan p){CareerMaterial m=p.getCareerMaterial();return new Response(p.getId(),p.getSession().getId(),false,m.getId(),m.getMaterialType().name(),p.getAuthoritySource().name(),p.getDisplayFilename(),p.getMimeType(),p.getCreatedAt());}
    private record Selection(CareerMaterial material,AuthoritySource source){}
    public record Response(Long id,Long sessionId,boolean userSelectionRequired,Long materialId,String materialType,String authoritySource,String displayFilename,String mimeType,java.time.OffsetDateTime createdAt){}
}
