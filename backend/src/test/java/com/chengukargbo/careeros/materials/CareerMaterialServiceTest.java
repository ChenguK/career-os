package com.chengukargbo.careeros.materials;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.io.*;import java.util.*;import java.util.zip.*;
import org.junit.jupiter.api.*;import org.junit.jupiter.api.extension.ExtendWith;import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;import org.springframework.mock.web.MockMultipartFile;import org.springframework.test.util.ReflectionTestUtils;
import com.chengukargbo.careeros.applications.ApplicationRepository;import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.jobs.RemoteType;import com.chengukargbo.careeros.profile.*;

@ExtendWith(MockitoExtension.class)
class CareerMaterialServiceTest {
 @Mock CareerMaterialRepository materials; @Mock ApplicantProfileRepository profiles;
 @Mock ApplicationRepository applications; @Mock CareerMaterialStorage storage;
 @InjectMocks CareerMaterialService service; ApplicantProfile profile;
 @BeforeEach void setup() throws Exception {
  profile=new ApplicantProfile("Ada","Lovelace",null,"ada@example.com",null,null,null,null,null,null,null,null,null,RemoteType.UNKNOWN,null,"USD",null,null);
  ReflectionTestUtils.setField(profile,"id",3L);
  lenient().when(profiles.findByProfileKey(ApplicantProfile.PRIMARY_PROFILE_KEY)).thenReturn(Optional.of(profile));
  lenient().when(materials.saveAndFlush(any())).thenAnswer(i->{CareerMaterial m=i.getArgument(0);ReflectionTestUtils.setField(m,"id",8L);ReflectionTestUtils.setField(m,"createdAt",java.time.OffsetDateTime.now());ReflectionTestUtils.setField(m,"updatedAt",java.time.OffsetDateTime.now());return m;});
  lenient().when(storage.store(any())).thenReturn("123e4567-e89b-12d3-a456-426614174000");
 }
 @Test void uploadsPdfWithExplicitProfileOwnership() throws Exception {var file=new MockMultipartFile("file","../Paid Résumé.pdf","application/octet-stream","%PDF-1.7 safe".getBytes());var result=service.upload(file,"General Resume","Operations","MID","v2",null);assertThat(result.applicantProfileId()).isEqualTo(3L);assertThat(result.mimeType()).isEqualTo("application/pdf");assertThat(result.originalFilename()).doesNotContain("/");verify(storage).store(any());}
 @Test void uploadsStructurallyValidDocx() throws Exception {var file=new MockMultipartFile("file","resume.docx","application/zip",docx());assertThat(service.upload(file,"Resume",null,null,null,null).mimeType()).contains("wordprocessingml");}
 @Test void rejectsExecutableDisguisedAsPdf(){var file=new MockMultipartFile("file","resume.pdf","application/pdf","MZ executable".getBytes());assertThatThrownBy(()->service.upload(file,"Resume",null,null,null,null)).isInstanceOf(BusinessValidationException.class).hasMessageContaining("valid PDF or DOCX");verifyNoInteractions(storage);}
 @Test void rejectsOversizedFile(){var file=new MockMultipartFile("file","resume.pdf","application/pdf",new byte[(int)CareerMaterialService.MAX_FILE_SIZE+1]);assertThatThrownBy(()->service.upload(file,"Resume",null,null,null,null)).hasMessageContaining("5 MB");}
 @Test void defaultChangeDoesNotTouchApplications(){CareerMaterial material=material(true);when(materials.findById(8L)).thenReturn(Optional.of(material));service.setDefault(8L);assertThat(profile.getDefaultResumeMaterial()).isSameAs(material);verifyNoInteractions(applications);}
 @Test void referencedMaterialCanOnlyBeArchived() throws Exception {CareerMaterial material=material(true);when(materials.findById(8L)).thenReturn(Optional.of(material));when(applications.existsByResumeMaterialId(8L)).thenReturn(true);assertThatThrownBy(()->service.delete(8L)).hasMessageContaining("archived instead");assertThat(service.deactivate(8L).active()).isFalse();verify(storage,never()).delete(any());}
 private CareerMaterial material(boolean active){CareerMaterial m=new CareerMaterial(profile,"Resume","resume.pdf","123e4567-e89b-12d3-a456-426614174000","application/pdf",10,null,null,null,null);ReflectionTestUtils.setField(m,"id",8L);ReflectionTestUtils.setField(m,"active",active);ReflectionTestUtils.setField(m,"createdAt",java.time.OffsetDateTime.now());ReflectionTestUtils.setField(m,"updatedAt",java.time.OffsetDateTime.now());return m;}
 private byte[] docx() throws IOException {ByteArrayOutputStream out=new ByteArrayOutputStream();try(ZipOutputStream zip=new ZipOutputStream(out)){zip.putNextEntry(new ZipEntry("[Content_Types].xml"));zip.write("types".getBytes());zip.closeEntry();zip.putNextEntry(new ZipEntry("word/document.xml"));zip.write("document".getBytes());zip.closeEntry();}return out.toByteArray();}
}
