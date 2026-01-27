package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.entity.Address;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {

    /**
     * Find all addresses of a specific account
     * 
     * @param accountId The account ID
     * @return List of addresses
     */
    List<Address> findByAccountId(Integer accountId);

    /**
     * Find the default address of a specific account
     * 
     * @param accountId The account ID
     * @return Optional of default address
     */
    Optional<Address> findByAccountIdAndIsDefaultTrue(Integer accountId);

    /**
     * Find address by ID and account ID (for ownership check)
     * 
     * @param id        Address ID
     * @param accountId Account ID
     * @return Optional of address
     */
    @Query("SELECT a FROM Address a WHERE a.id = :id AND a.account.id = :accountId")
    Optional<Address> findByIdAndAccountId(@Param("id") Integer id, @Param("accountId") Integer accountId);

    /**
     * Count total addresses of a specific account
     * 
     * @param accountId The account ID
     * @return Number of addresses
     */
    long countByAccountId(Integer accountId);

    /**
     * Unset all default addresses for a specific account
     * 
     * @param accountId The account ID
     */
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.account.id = :accountId AND a.isDefault = true")
    void unsetAllDefaultsByAccountId(@Param("accountId") Integer accountId);
}
