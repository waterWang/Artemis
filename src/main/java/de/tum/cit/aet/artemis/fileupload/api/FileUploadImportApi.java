package de.tum.cit.aet.artemis.fileupload.api;

import java.util.Optional;

import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import de.tum.cit.aet.artemis.core.exception.NoUniqueQueryException;
import de.tum.cit.aet.artemis.fileupload.config.FileUploadEnabled;
import de.tum.cit.aet.artemis.fileupload.domain.FileUploadExercise;
import de.tum.cit.aet.artemis.fileupload.repository.FileUploadExerciseRepository;
import de.tum.cit.aet.artemis.fileupload.service.FileUploadExerciseImportService;

/**
 * API for functionality regarding the import of file upload exercises (but not for general upload functionality).
 */
@Conditional(FileUploadEnabled.class)
@Controller
@Lazy
public class FileUploadImportApi extends AbstractFileModuleApi {

    private final FileUploadExerciseRepository fileUploadExerciseRepository;

    private final FileUploadExerciseImportService fileUploadExerciseImportService;

    public FileUploadImportApi(FileUploadExerciseRepository fileUploadExerciseRepository, FileUploadExerciseImportService fileUploadExerciseImportService) {
        this.fileUploadExerciseRepository = fileUploadExerciseRepository;
        this.fileUploadExerciseImportService = fileUploadExerciseImportService;
    }

    public Optional<FileUploadExercise> findUniqueWithCompetenciesByTitleAndCourseId(String title, long courseId) throws NoUniqueQueryException {
        return fileUploadExerciseRepository.findUniqueWithCompetenciesByTitleAndCourseId(title, courseId);
    }

    public FileUploadExercise findWithGradingCriteriaByIdElseThrow(Long exerciseId) {
        return fileUploadExerciseRepository.findWithGradingCriteriaByIdElseThrow(exerciseId);
    }

    /**
     * Loads the source file upload exercise with its competency links, needed to preserve them during exam import. The
     * grading criteria are loaded separately by the caller (see ExamImportService), and the plagiarism detection config
     * is intentionally not loaded because the import never copies it onto exam exercises (see
     * ExerciseImportService#copyExerciseBasis).
     *
     * @param sourceExerciseId the id of the source exercise
     * @return the source exercise with its competency links, or empty if it no longer exists
     */
    public Optional<FileUploadExercise> findWithExamImportBasisById(long sourceExerciseId) {
        return fileUploadExerciseRepository.findWithCompetencyLinksById(sourceExerciseId);
    }

    public FileUploadExercise importFileUploadExercise(final FileUploadExercise templateExercise, FileUploadExercise importedExercise) {
        return fileUploadExerciseImportService.importFileUploadExercise(templateExercise, importedExercise);
    }

    public Optional<FileUploadExercise> importFileUploadExercise(final long exerciseToCopyId, final FileUploadExercise importedExercise) {
        final Optional<FileUploadExercise> optionalFileUploadExercise = fileUploadExerciseRepository
                .findByIdWithExampleSubmissionsAndResultsAndCompetenciesAndGradingCriteria(exerciseToCopyId);
        return optionalFileUploadExercise.map(templateExercise -> fileUploadExerciseImportService.importFileUploadExercise(templateExercise, importedExercise));
    }
}
