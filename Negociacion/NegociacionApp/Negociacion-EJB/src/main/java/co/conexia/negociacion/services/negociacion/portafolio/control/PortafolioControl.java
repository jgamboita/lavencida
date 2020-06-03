package co.conexia.negociacion.services.negociacion.portafolio.control;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * Control de portafolio
 * @author dchapeton
 *
 */
public class PortafolioControl {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    /**
     * Metodo para crear un portafolio nuevo cuando la sede no lo tiene
     * (debido a que portafolio se desacoplo y las sedes nuevas no lo traen)
     * @param sedeId sede prestador id
     */
    public void crearPortafolioASedePrestadorSinPortafolio(Long sedeId) {
        StringBuilder sql = new StringBuilder();

        sql.append("INSERT INTO contratacion.portafolio (fecha_insert) ")
                .append("SELECT current_date ")
                .append("FROM contratacion.sede_prestador sp ")
                .append("WHERE sp.id = :sedeId ")
                .append("AND sp.portafolio_id IS NULL ")
                .append("RETURNING id");

        List<Long> result = em.createNativeQuery(sql.toString())
                .setParameter("sedeId", sedeId)
                .getResultList();

        if(!result.isEmpty()) {
            sql = new StringBuilder();
            sql.append("UPDATE contratacion.sede_prestador SET portafolio_id = :portafolioId ")
                    .append("WHERE id = :sedeId ")
                    .append("AND portafolio_id IS NULL ");

            em.createNativeQuery(sql.toString())
                    .setParameter("sedeId", sedeId)
                    .setParameter("portafolioId", result.get(0))
                    .executeUpdate();
        }
    }

}
