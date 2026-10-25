package com.ruon2.rssapi;

import java.util.List;

public class Sample {

    public static void main(String[] params) {

        if (params.length != 2) {
            System.err.println("Please execute me with two params: <email> <password>");
            return;
        }

        /**
         * If you need a different HTTP implementation (for instance an async implementation),
         * you need to implement your own http.Get and call:
         *
         *    Http.setGetImplementation(new MyAsyncGet());
         *
         */

        API.authenticate(params[0], params[1], new API.AuthenticateListener() {

            @Override
            public void error(Exception e) {
                // Handle authentication error
                System.err.println(e);
            }

            @Override
            protected void authenticated(String guid) {
                // On successful authentication, you might want to
                // save the guid in a secure place for future executions.

                API.alarms(guid, false, new API.FetchAlarmsListener() {
                    @Override
                    public void error(Exception e) {
                        // Handle alarms fetch error
                        System.err.println(e);
                    }

                    @Override
                    protected void alarms(List<Alarm> alarms) {
                        // Display the list of alarms

                        if (alarms.size() == 0) {
                            System.out.println("There are no alarms :-)");
                        } else {
                            for (Alarm alarm : alarms) {
                                System.out.println(alarm);
                            }
                        }
                    }
                });
            }
        });

    }
}
