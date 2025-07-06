package com.example.taskmanager.data.remote;

import com.example.taskmanager.data.local.model.TaskEntity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class FirebaseService {

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public FirebaseService() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }


    // Sign up
    public void signUp(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    //  Google Sign-In
    public void firebaseAuthWithGoogle(String idToken, OnCompleteListener<AuthResult> listener) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential).addOnCompleteListener(listener);
    }

    // Sign in
    public void signIn(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    //  Add task for user
    public void addTask(TaskEntity task, OnCompleteListener<DocumentReference> listener) {
        String userId = getUserId();
        if (userId == null) return;

        firestore.collection("tasks")
                .document(userId)
                .collection("user_tasks")
                .add(task)
                .addOnCompleteListener(listener);
    }

    //  Get all tasks for user (real-time listener)
    public void getAllTasks(EventListener<QuerySnapshot> listener) {
        String userId = getUserId();
        if (userId == null) return;

        firestore.collection("tasks")
                .document(userId)
                .collection("user_tasks")
                .addSnapshotListener(listener);
    }

    //  Update task by Firestore doc ID
    public void updateTask(String taskId, TaskEntity updatedTask, OnCompleteListener<Void> listener) {
        String userId = getUserId();
        if (userId == null) return;

        firestore.collection("tasks")
                .document(userId)
                .collection("user_tasks")
                .document(taskId)
                .set(updatedTask)
                .addOnCompleteListener(listener);
    }

    //  Helper to get logged-in user ID
    private String getUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }

    public void deleteTask(String taskId, OnCompleteListener<Void> listener) {
        String userId = getUserId();
        if (userId == null) return;

        firestore.collection("tasks")
                .document(userId)
                .collection("user_tasks")
                .document(taskId)
                .delete()
                .addOnCompleteListener(listener);
    }

}

