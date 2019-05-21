stream-muxer is the base module in terms of hierarchy.
Looking at the Go implementation you can see that:
- Transport.Conn extends smux.Conn
- Network.Conn extends smux.Conn

We may take a different path in this implementation, this is still TBD.

Reference:
https://github.com/libp2p/go-stream-muxer/blob/master/muxer.go
