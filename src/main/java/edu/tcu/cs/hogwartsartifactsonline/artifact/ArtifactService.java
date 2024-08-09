package edu.tcu.cs.hogwartsartifactsonline.artifact;

import edu.tcu.cs.hogwartsartifactsonline.artifact.dto.ArtifactDto;
import edu.tcu.cs.hogwartsartifactsonline.artifact.utils.IdWorker;
import edu.tcu.cs.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ArtifactService {

    private final ArtifactRepository artifactRepository;

    private final IdWorker idWorker;


    public ArtifactService(ArtifactRepository artifactRepository, IdWorker idWorker) {
        this.artifactRepository = artifactRepository;
        this.idWorker = idWorker;
    }

    public Artifact findById(String artifactId){
        return this.artifactRepository.findById(artifactId)
                .orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));
    }

    public List<Artifact> findAll(){
        return this.artifactRepository.findAll();
    }

    public Artifact save(Artifact newArtifact){
        newArtifact.setId(idWorker.nextId() + "");
        return this.artifactRepository.save(newArtifact);
    }

    public Artifact update(String artifactId, Artifact update){
        return this.artifactRepository.findById(artifactId)
                .map(oldArtifact -> {
                    oldArtifact.setName(update.getName());
                    oldArtifact.setDescription(update.getDescription());
                    oldArtifact.setImageUrl(update.getImageUrl());
                    return this.artifactRepository.save(oldArtifact);
                })
                .orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));
    }

    public void delete(String artifactId){
        this.artifactRepository.findById(artifactId)
                .orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));
        this.artifactRepository.deleteById(artifactId);
    }

    public Page<Artifact> findAll(Pageable pageable) {
        return this.artifactRepository.findAll(pageable);
    }

    public Page<Artifact> findByCriteria(Map<String, String> searchCriteria, Pageable pageable) {
        Specification<Artifact> spec = Specification.where(null);

        if(StringUtils.hasLength(searchCriteria.get("id"))){
            spec = spec.and(ArtifactSpecs.hasId(searchCriteria.get("id")));
        }

        if(StringUtils.hasLength(searchCriteria.get("name"))){
            spec = spec.and(ArtifactSpecs.containsName(searchCriteria.get("name")));
        }

        if(StringUtils.hasLength(searchCriteria.get("description"))){
            spec = spec.and(ArtifactSpecs.containsDescription(searchCriteria.get("description")));
        }

        if(StringUtils.hasLength(searchCriteria.get("ownerName"))){
            spec = spec.and(ArtifactSpecs.hasOwnerName(searchCriteria.get("ownerName")));
        }

        return this.artifactRepository.findAll(spec, pageable);
    }

}
