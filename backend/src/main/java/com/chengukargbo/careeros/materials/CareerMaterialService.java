package com.chengukargbo.careeros.materials;

import java.io.*;
import java.util.*;
import java.util.zip.ZipInputStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.chengukargbo.careeros.applications.ApplicationRepository;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.materials.CareerMaterialDtos.Response;
import com.chengukargbo.careeros.profile.*;

@Service @Transactional
public class CareerMaterialService {
    static final long MAX_FILE_SIZE=5L*1024*1024;
    private static final String PDF="application/pdf";
    private static final String DOCX="application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private final CareerMaterialRepository materials; private final ApplicantProfileRepository profiles;
    private final ApplicationRepository applications; private final CareerMaterialStorage storage;

    public CareerMaterialService(CareerMaterialRepository materials, ApplicantProfileRepository profiles,
        ApplicationRepository applications, CareerMaterialStorage storage) {
        this.materials=materials; this.profiles=profiles; this.applications=applications; this.storage=storage;
    }

    @Transactional(readOnly=true) public List<Response> list(){ApplicantProfile p=profile();
        return materials.findByApplicantProfileIdOrderByActiveDescDisplayNameAscIdAsc(p.getId()).stream().map(m->response(m,p)).toList();}

    public Response upload(MultipartFile file,String displayName,String targetJobFamily,
        String targetSeniority,String versionLabel,String notes) {
        ApplicantProfile profile=profile();
        if(file==null||file.isEmpty()) throw new BusinessValidationException("Resume file is required");
        if(file.getSize()>MAX_FILE_SIZE) throw new BusinessValidationException("Resume file exceeds the 5 MB limit");
        String original=sanitize(file.getOriginalFilename()); String mime=validate(original,file);
        String key=null;
        try {
            key=storage.store(file.getInputStream());
            CareerMaterial material=new CareerMaterial(profile,limited(required(displayName,"Display name is required"),200,"Display name"),
                original,key,mime,file.getSize(),normalize(notes),limited(normalize(targetJobFamily),120,"Target job family"),
                limited(normalize(targetSeniority),80,"Target seniority"),limited(normalize(versionLabel),100,"Version label"));
            return response(materials.saveAndFlush(material),profile);
        } catch(IOException ex) {
            if(key!=null) try { storage.delete(key); } catch(IOException ignored) { }
            throw new BusinessValidationException("Resume file could not be stored");
        }
    }

    public Response setDefault(Long id){ApplicantProfile p=profile();CareerMaterial m=owned(id,p);
        if(!m.isActive()) throw new BusinessValidationException("Only an active resume can be the profile default");
        p.setDefaultResumeMaterial(m); profiles.saveAndFlush(p); return response(m,p);}

    public Response deactivate(Long id){ApplicantProfile p=profile();CareerMaterial m=owned(id,p);m.deactivate();
        if(p.getDefaultResumeMaterial()!=null&&p.getDefaultResumeMaterial().getId().equals(id))p.setDefaultResumeMaterial(null);
        materials.save(m);profiles.saveAndFlush(p);return response(m,p);}

    public void delete(Long id){ApplicantProfile p=profile();CareerMaterial m=owned(id,p);
        if(applications.existsByResumeMaterialId(id))throw new BusinessValidationException("Resume is referenced by an application and must be archived instead");
        if(p.getDefaultResumeMaterial()!=null&&p.getDefaultResumeMaterial().getId().equals(id))p.setDefaultResumeMaterial(null);
        profiles.saveAndFlush(p);materials.delete(m);
        try{storage.delete(m.getStorageKey());}catch(IOException ex){throw new BusinessValidationException("Resume file could not be removed");}
    }

    @Transactional(readOnly=true) public Download download(Long id){ApplicantProfile p=profile();CareerMaterial m=owned(id,p);
        try {CareerMaterialStorage.StoredMaterial stored=storage.read(m.getStorageKey());
            return new Download(stored.content().readAllBytes(),m.getMimeType(),m.getOriginalFilename());
        } catch(IOException ex){throw new BusinessValidationException("Resume file is unavailable");}}

    public CareerMaterial requireOwnedActive(Long id){CareerMaterial m=owned(id,profile());if(!m.isActive())
        throw new BusinessValidationException("Selected resume is archived");return m;}
    private ApplicantProfile profile(){return profiles.findByProfileKey(ApplicantProfile.PRIMARY_PROFILE_KEY)
        .orElseThrow(()->new BusinessValidationException("Save the applicant profile before adding career materials"));}
    private CareerMaterial owned(Long id,ApplicantProfile p){CareerMaterial m=materials.findById(id)
        .orElseThrow(()->new BusinessValidationException("Resume material not found"));
        if(!m.getApplicantProfile().getId().equals(p.getId()))throw new BusinessValidationException("Resume material does not belong to the applicant profile");return m;}
    private Response response(CareerMaterial m,ApplicantProfile p){return new Response(m.getId(),p.getId(),m.getMaterialType(),m.getDisplayName(),m.getOriginalFilename(),m.getMimeType(),m.getFileSize(),m.isActive(),m.getNotes(),m.getTargetJobFamily(),m.getTargetSeniority(),m.getVersionLabel(),p.getDefaultResumeMaterial()!=null&&p.getDefaultResumeMaterial().getId().equals(m.getId()),m.getCreatedAt(),m.getUpdatedAt());}
    private String validate(String name,MultipartFile file){String lower=name.toLowerCase(Locale.ROOT);byte[] data;
        try{data=file.getBytes();}catch(IOException ex){throw new BusinessValidationException("Resume file could not be read");}
        if(lower.endsWith(".pdf")&&data.length>=5&&new String(data,0,5,java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-"))return PDF;
        if(lower.endsWith(".docx")&&isDocx(data))return DOCX;
        throw new BusinessValidationException("Resume must be a valid PDF or DOCX file");}
    private boolean isDocx(byte[] data){boolean content=false,document=false;try(ZipInputStream zip=new ZipInputStream(new ByteArrayInputStream(data))){for(var e=zip.getNextEntry();e!=null;e=zip.getNextEntry()){if("[Content_Types].xml".equals(e.getName()))content=true;if("word/document.xml".equals(e.getName()))document=true;}}catch(IOException ex){return false;}return content&&document;}
    private String sanitize(String value){String name=value==null?"resume":value.replace('\\','/');name=name.substring(name.lastIndexOf('/')+1).replaceAll("[^A-Za-z0-9._ -]","_").replaceAll("\\.{2,}",".").trim();return name.isBlank()?"resume":name.substring(0,Math.min(name.length(),255));}
    private String normalize(String value){return value==null||value.isBlank()?null:value.trim();}
    private String required(String value,String message){String n=normalize(value);if(n==null)throw new BusinessValidationException(message);return n;}
    private String limited(String value,int max,String label){if(value!=null&&value.length()>max)throw new BusinessValidationException(label+" must not exceed "+max+" characters");return value;}
    public record Download(byte[] content,String mimeType,String filename){}
}
