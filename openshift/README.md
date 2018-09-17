**Deploy JBoss EAP 6.4 Template**

`oc process -f eap-64-clustering-test.json -p app=eap-clustering-test | oc apply -f -`

**Deploy JBoss EAP 7.1 Template**

`oc process -f eap-71-clustering-test.json -p app=eap-clustering-test | oc apply -f -`
