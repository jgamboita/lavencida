package co.conexia.negociacion.filtros;



import com.conexia.contratacion.commons.dto.paginacion.FiltroTableLazyAbstract;

import javax.persistence.criteria.*;

public abstract class FiltroTableLazyProvider<F extends FiltroTableLazyAbstract, T> {

    protected CriteriaQuery<?> criteriaQuery;
    protected CriteriaBuilder criteriaBuilder;
    protected Root<T> root;
    protected Join<?, ?>[] joins;

    public FiltroTableLazyProvider(CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder, Root<T> root) {
        this.criteriaQuery = criteriaQuery;
        this.criteriaBuilder = criteriaBuilder;
        this.root = root;
    }

    public FiltroTableLazyProvider(CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder, Root<T> root, Join<?, ?>... joins) {
        this.criteriaQuery = criteriaQuery;
        this.criteriaBuilder = criteriaBuilder;
        this.root = root;
        this.joins = joins;
    }

    public abstract void setFiltro(F filtros);

}
