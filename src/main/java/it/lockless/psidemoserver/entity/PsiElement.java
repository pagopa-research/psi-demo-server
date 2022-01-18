package it.lockless.psidemoserver.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "psi_element")
public class PsiElement  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "psi_element_id_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name="psi_element_id_seq",sequenceName="psi_element_id_seq", allocationSize = 50)
    @Column(name = "id")
    private long id;

    @Column(name = "source")
    private String source;

    @Column(name = "value", nullable = false)
    private String value;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PsiElement that = (PsiElement) o;
        return id == that.id &&
                Objects.equals(source, that.source) &&
                value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, source, value);
    }
}
