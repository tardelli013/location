package br.com.tardelli.location.service;

import br.com.tardelli.location.Exceptions.BadRequestException;
import br.com.tardelli.location.model.LogLocalidade;
import br.com.tardelli.location.utils.LocationUtils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
public class LogLocalidadeService {

    public List<LogLocalidade> findLogLocalidade(LogLocalidade filter) {

        if (Objects.isNull(filter.getCity()) || Objects.isNull(filter.getState()) || Objects.isNull(filter.getUf())) {
            throw new BadRequestException(LocationUtils.getJsonRequiredFields("city","state","uf"));
        }

        Firestore db = FirestoreClient.getFirestore();
        CollectionReference location = db.collection("location");

        Query query = location
                .whereGreaterThanOrEqualTo("city", filter.getCity().trim().toLowerCase())
                .whereEqualTo("state", filter.getState().trim().toLowerCase())
                .whereEqualTo("uf", filter.getUf().trim().toLowerCase())
                .limit(1);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<LogLocalidade> r = new ArrayList<>();
        mapLogLocalidade(querySnapshot, r);

        return r;
    }

    private void mapLogLocalidade(ApiFuture<QuerySnapshot> querySnapshot, List<LogLocalidade> r) {

        try {
            for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
                LogLocalidade logLocalidade = new LogLocalidade();
                logLocalidade.setDocumentId(document.getId());
                logLocalidade.setCity(document.contains("city") ? LocationUtils.capitalizeString(document.getString("city")) : null);
                logLocalidade.setLoc_nu(document.contains("loc_nu") ? Objects.requireNonNull(document.getLong("loc_nu")).intValue() : null);
                logLocalidade.setPostal_code(document.contains("postal_code") ? document.getString("postal_code") : null);
                logLocalidade.setState(document.contains("state") ? LocationUtils.capitalizeString(document.getString("state")) : null);
                logLocalidade.setUf(document.contains("uf") ? Objects.requireNonNull(document.getString("uf")).toUpperCase() : null);

                r.add(logLocalidade);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

}
