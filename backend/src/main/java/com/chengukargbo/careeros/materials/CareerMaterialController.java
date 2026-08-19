package com.chengukargbo.careeros.materials;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.chengukargbo.careeros.materials.CareerMaterialDtos.Response;

@RestController @RequestMapping("/api/applicant-profile/materials")
public class CareerMaterialController {
    private final CareerMaterialService service;
    public CareerMaterialController(CareerMaterialService service){this.service=service;}
    @GetMapping public List<Response> list(){return service.list();}
    @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public Response upload(
        @RequestPart("file") MultipartFile file,@RequestParam String displayName,
        @RequestParam(required=false) String targetJobFamily,@RequestParam(required=false) String targetSeniority,
        @RequestParam(required=false) String versionLabel,@RequestParam(required=false) String notes){return service.upload(file,displayName,targetJobFamily,targetSeniority,versionLabel,notes);}
    @PostMapping("/{id}/default") public Response setDefault(@PathVariable Long id){return service.setDefault(id);}
    @PostMapping("/{id}/deactivate") public Response deactivate(@PathVariable Long id){return service.deactivate(id);}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id){service.delete(id);}
    @GetMapping("/{id}/download") public ResponseEntity<byte[]> download(@PathVariable Long id){var d=service.download(id);
        ContentDisposition disposition=ContentDisposition.attachment().filename(d.filename(),StandardCharsets.UTF_8).build();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(d.mimeType())).header(HttpHeaders.CONTENT_DISPOSITION,disposition.toString()).body(d.content());}
}
