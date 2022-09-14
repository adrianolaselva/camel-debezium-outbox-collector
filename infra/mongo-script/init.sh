mongo -- config <<EOF

    var user = 'root';
    var password = 'root';

    use outbox
    db.createUser({user: user, pwd: password, roles: ["readWrite"]});
    db.createCollection("outbox");

EOF