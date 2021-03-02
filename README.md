# Streaming Word Counter with oslib, cats-effect and fs2

This application will stream events and do a windowed word count by event type and word.
Events are streamed from a binary ("blackbox") in the form:

```json
{
 "event_type": "foo",
 "data": "bar",
 "timestamp": 1234
}
```

The application will aggregate these events in a second window and count words by event type:

```json
{
    "type1": {
        "data1": 2,
        "data2": 2
    },
    "type2": {
        "data1": 1,
        "data2": 1
    }
}
```

The current word count is exposed through an HTTP endpoint at `GET http://127.0.0.1:8080/`

To run, add the binary to your path:

```bash
export PATH=/path/to/blackbox:$PATH
```

and run the application:
```bash
sbt run
```