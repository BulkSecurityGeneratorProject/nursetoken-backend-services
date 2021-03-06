package com.allcode.nursetoken.domain;

import com.allcode.nursetoken.service.util.CryptUtils;
import com.allcode.nursetoken.service.util.MiddlewareRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;
import org.springframework.boot.jackson.JsonObjectDeserializer;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * A Wallet.
 */
@Entity
@Table(name = "wallet")
public class Wallet extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "address", nullable = false)
    private String address;

    @NotNull
    @Column(name = "encripted_private_key", nullable = false, updatable = false)
    @JsonIgnore
    private String encriptedPrivateKey;

    @NotNull
    @Column(name = "encripted_public_key", nullable = false, updatable = false)
    @JsonIgnore
    private String encriptedPublicKey;

    @NotNull
    @Column(name = "encripted_public_key_hash", nullable = false, updatable = false)
    @JsonIgnore
    private String encriptedPublicKeyHash;

    @NotNull
    @Column(name = "encripted_wif", nullable = false, updatable = false)
    @JsonIgnore
    private String encriptedWif;

    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties("")
    private User owner;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public Wallet address(String address) {
        this.address = address;
        return this;
    }

    public static Wallet createFromApiNeo(User user){
        String key = System.getenv("PASSPHRASE_VALUE");
        String url = System.getenv("NEO_API_URL") + "/wallet/new";
        JSONObject response = MiddlewareRequest.post(url, null);

        if(!response.has("address")){
            return null;
        }

        CryptUtils.encrypt(response.getString("address"), key);
        Wallet wallet = new Wallet();
        wallet.setAddress(response.getString("address"));
        wallet.setEncriptedPrivateKey(CryptUtils.encrypt(response.getString("private_key"), key));
        wallet.setEncriptedPublicKey(CryptUtils.encrypt(response.getString("public_key"), key));
        wallet.setEncriptedPublicKeyHash(CryptUtils.encrypt(response.getString("public_key_hash"), key));
        wallet.setEncriptedWif(CryptUtils.encrypt(response.getString("wif"), key));
        wallet.setOwner(user);
        wallet.setName(user.getFirstName() + "'s Wallet");
        return wallet;
    }

    public static Wallet importWifFromApiNeo(ImportableWallet importableWallet, User user){
        String key = System.getenv("PASSPHRASE_VALUE");
        String url = System.getenv("NEO_API_URL") + "/get_data_from_wif/" + importableWallet.getWif();
        JSONObject response = MiddlewareRequest.get(url);

        if(!response.has("address")){
            return null;
        }

        CryptUtils.encrypt(response.getString("address"), key);
        Wallet wallet = new Wallet();
        wallet.setAddress(response.getString("address"));
        wallet.setEncriptedPrivateKey(CryptUtils.encrypt(response.getString("private_key"), key));
        wallet.setEncriptedPublicKey(CryptUtils.encrypt(response.getString("public_key"), key));
        wallet.setEncriptedPublicKeyHash(CryptUtils.encrypt(response.getString("public_key_hash"), key));
        wallet.setEncriptedWif(CryptUtils.encrypt(response.getString("wif"), key));
        wallet.setOwner(user);
        wallet.setName(importableWallet.getName());
        return wallet;
    }

    @JsonIgnore
    public ObjectNode getBalance(List<Token> tokens){
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode balance = mapper.createObjectNode();
        JSONObject data = MiddlewareRequest.get(System.getenv("NEO_API_URL") + "/accountstate/" + this.getAddress());

        if(!data.has("response")){
            balance.put("NEO", 0);
            balance.put("GAS", 0);
        }else{
            JSONObject balances = data.getJSONObject("response").getJSONObject("balances");

            if(balances.has("neo")){
                balance.put("NEO", balances.getBigInteger("neo"));
            }else{
                balance.put("NEO", 0);
            }

            if(balances.has("gas")){
                balance.put("GAS", balances.getBigInteger("gas"));
            }else{
                balance.put("GAS", 0);
            }
        }

        if(tokens.size() == 0){
            return balance;
        }


        for(Token token: tokens){
            data = MiddlewareRequest.get(
                System.getenv("NEO_API_URL") + "/get_token_balance/" + token.getScriptHash() + "/" + this.getAddress()
            );

            if(!data.has("response")){
                balance.put(token.getSymbol(), 0);
            }else{
                JSONObject response = data.getJSONObject("response");
                balance.put(token.getSymbol(), response.getBigInteger("value"));
            }
        }

        return balance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEncriptedPrivateKey() {
        return encriptedPrivateKey;
    }

    public Wallet encriptedPrivateKey(String encriptedPrivateKey) {
        this.encriptedPrivateKey = encriptedPrivateKey;
        return this;
    }

    public void setEncriptedPrivateKey(String encriptedPrivateKey) {
        this.encriptedPrivateKey = encriptedPrivateKey;
    }

    public String getEncriptedPublicKey() {
        return encriptedPublicKey;
    }

    public Wallet encriptedPublicKey(String encriptedPublicKey) {
        this.encriptedPublicKey = encriptedPublicKey;
        return this;
    }

    public void setEncriptedPublicKey(String encriptedPublicKey) {
        this.encriptedPublicKey = encriptedPublicKey;
    }

    public String getEncriptedPublicKeyHash() {
        return encriptedPublicKeyHash;
    }

    public Wallet encriptedPublicKeyHash(String encriptedPublicKeyHash) {
        this.encriptedPublicKeyHash = encriptedPublicKeyHash;
        return this;
    }

    public void setEncriptedPublicKeyHash(String encriptedPublicKeyHash) {
        this.encriptedPublicKeyHash = encriptedPublicKeyHash;
    }

    public String getEncriptedWif() {
        return encriptedWif;
    }

    public Wallet encriptedWif(String encriptedWif) {
        this.encriptedWif = encriptedWif;
        return this;
    }

    public void setEncriptedWif(String encriptedWif) {
        this.encriptedWif = encriptedWif;
    }

    public User getOwner() {
        return owner;
    }

    public Wallet owner(User user) {
        this.owner = user;
        return this;
    }

    public void setOwner(User user) {
        this.owner = user;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Wallet wallet = (Wallet) o;
        if (wallet.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), wallet.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Wallet{" +
            "id=" + getId() +
            ", address='" + getAddress() + "'" +
            ", privateKey='" + getEncriptedPrivateKey() + "'" +
            ", publicKey='" + getEncriptedPublicKey() + "'" +
            ", publicKeyHash='" + getEncriptedPublicKey() + "'" +
            ", wif='" + getEncriptedWif() + "'" +
            "}";
    }
}
