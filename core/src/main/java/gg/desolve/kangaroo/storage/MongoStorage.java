package gg.desolve.kangaroo.storage;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import lombok.Getter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.bson.Document;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MongoStorage {

    private final MongoClient client;
    @Getter
    private final MongoDatabase database;

    public MongoStorage(String uri, String databaseName) {
        Configurator.setLevel("org.mongodb.driver", Level.ERROR);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .build();

        this.client = MongoClients.create(settings);
        this.database = client.getDatabase(databaseName);
    }

    public MongoCollection<Document> getCollection(String name) {
        return database.getCollection(name);
    }

    public <T> CompletableFuture<T> toFuture(Publisher<T> publisher) {
        CompletableFuture<T> future = new CompletableFuture<>();

        publisher.subscribe(new Subscriber<>() {
            private T value;

            @Override
            public void onSubscribe(Subscription subscription) {
                subscription.request(1);
            }

            @Override
            public void onNext(T item) {
                this.value = item;
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                future.complete(value);
            }
        });

        return future;
    }

    public <T> CompletableFuture<List<T>> toFutureList(Publisher<T> publisher) {
        CompletableFuture<List<T>> future = new CompletableFuture<>();
        List<T> results = new ArrayList<>();

        publisher.subscribe(new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T item) {
                results.add(item);
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                future.complete(results);
            }
        });

        return future;
    }

    public void close() {
        client.close();
    }
}
