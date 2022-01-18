package it.lockless.psidemoserver.mapper;

/**
 * A rest mapper converter. Only the conversion from ENTITY to DTO is necessary for this class of mappers.
 */
public interface ControllerMapper<ENTITY, DTO> {

    /**
     * Convert an ENTITY to a DTO.
     *
     * @param entity to convert
     * @return the dto
     */
    DTO toDto(ENTITY entity);
}
