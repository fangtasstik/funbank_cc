// Funbank MongoDB Event Store Initialization Script
// Sets up event sourcing collections and indexes for banking event storage

// Switch to the funbank_events database
db = db.getSiblingDB('funbank_events');

// Create application user with appropriate permissions
db.createUser({
  user: "funbank_events_user",
  pwd: "funbank_events_pass_2024",
  roles: [
    {
      role: "readWrite",
      db: "funbank_events"
    }
  ]
});

// Create event_store collection for event sourcing
// This collection stores all domain events in the banking system
db.createCollection("event_store", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["aggregateId", "eventType", "eventData", "eventVersion", "timestamp"],
      properties: {
        aggregateId: {
          bsonType: "string",
          description: "Unique identifier for the aggregate (e.g., user ID, account ID)"
        },
        aggregateType: {
          bsonType: "string",
          description: "Type of aggregate (User, Account, Transaction, etc.)"
        },
        eventType: {
          bsonType: "string",
          description: "Type of event (UserCreated, AccountOpened, TransactionProcessed, etc.)"
        },
        eventData: {
          bsonType: "object",
          description: "Event payload containing all relevant data"
        },
        eventVersion: {
          bsonType: "int",
          minimum: 1,
          description: "Version of the event for aggregate consistency"
        },
        timestamp: {
          bsonType: "date",
          description: "When the event occurred"
        },
        metadata: {
          bsonType: "object",
          description: "Additional metadata like correlation ID, user context, etc."
        }
      }
    }
  }
});

// Create indexes for optimal query performance in banking scenarios
db.event_store.createIndex({ "aggregateId": 1, "eventVersion": 1 }, { unique: true });
db.event_store.createIndex({ "aggregateId": 1, "timestamp": 1 });
db.event_store.createIndex({ "eventType": 1, "timestamp": 1 });
db.event_store.createIndex({ "aggregateType": 1, "timestamp": 1 });
db.event_store.createIndex({ "timestamp": 1 });
db.event_store.createIndex({ "metadata.correlationId": 1 });

// Create snapshots collection for event sourcing performance optimization
db.createCollection("snapshots", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["aggregateId", "aggregateVersion", "snapshotData", "timestamp"],
      properties: {
        aggregateId: {
          bsonType: "string",
          description: "Unique identifier for the aggregate"
        },
        aggregateType: {
          bsonType: "string",
          description: "Type of aggregate being snapshotted"
        },
        aggregateVersion: {
          bsonType: "int",
          minimum: 1,
          description: "Version of the aggregate at snapshot time"
        },
        snapshotData: {
          bsonType: "object",
          description: "Complete state of the aggregate at snapshot time"
        },
        timestamp: {
          bsonType: "date",
          description: "When the snapshot was taken"
        }
      }
    }
  }
});

// Create indexes for snapshot queries
db.snapshots.createIndex({ "aggregateId": 1, "aggregateVersion": -1 });
db.snapshots.createIndex({ "aggregateType": 1, "timestamp": -1 });

// Create projection_states collection for CQRS read models
// This stores the current state of various projections (views) built from events
db.createCollection("projection_states", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["projectionName", "lastProcessedEvent", "state"],
      properties: {
        projectionName: {
          bsonType: "string",
          description: "Name of the projection (UserProfile, AccountSummary, etc.)"
        },
        lastProcessedEvent: {
          bsonType: "object",
          properties: {
            aggregateId: { bsonType: "string" },
            eventVersion: { bsonType: "int" },
            timestamp: { bsonType: "date" }
          },
          description: "Last event processed by this projection"
        },
        state: {
          bsonType: "object",
          description: "Current state of the projection"
        },
        updatedAt: {
          bsonType: "date",
          description: "When the projection state was last updated"
        }
      }
    }
  }
});

// Create indexes for projection state management
db.projection_states.createIndex({ "projectionName": 1 }, { unique: true });
db.projection_states.createIndex({ "updatedAt": -1 });

// Create audit_trail collection for banking compliance
// This collection maintains an immutable audit trail of all system activities
db.createCollection("audit_trail", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["userId", "action", "timestamp", "ipAddress"],
      properties: {
        userId: {
          bsonType: "string",
          description: "ID of the user who performed the action"
        },
        action: {
          bsonType: "string",
          description: "Action performed (LOGIN, LOGOUT, TRANSFER, etc.)"
        },
        resourceType: {
          bsonType: "string",
          description: "Type of resource affected"
        },
        resourceId: {
          bsonType: "string",
          description: "ID of the resource affected"
        },
        details: {
          bsonType: "object",
          description: "Additional details about the action"
        },
        timestamp: {
          bsonType: "date",
          description: "When the action occurred"
        },
        ipAddress: {
          bsonType: "string",
          description: "IP address from which the action was performed"
        },
        userAgent: {
          bsonType: "string",
          description: "User agent string"
        },
        correlationId: {
          bsonType: "string",
          description: "Correlation ID for tracking related actions"
        }
      }
    }
  }
});

// Create indexes for audit trail queries
db.audit_trail.createIndex({ "userId": 1, "timestamp": -1 });
db.audit_trail.createIndex({ "action": 1, "timestamp": -1 });
db.audit_trail.createIndex({ "resourceType": 1, "resourceId": 1, "timestamp": -1 });
db.audit_trail.createIndex({ "timestamp": -1 });
db.audit_trail.createIndex({ "correlationId": 1 });

// Insert initial system events for tracking
db.event_store.insertOne({
  aggregateId: "SYSTEM",
  aggregateType: "System",
  eventType: "SystemInitialized",
  eventData: {
    version: "1.0.0",
    environment: "development",
    initializedBy: "admin"
  },
  eventVersion: 1,
  timestamp: new Date(),
  metadata: {
    correlationId: "SYSTEM-INIT-001",
    source: "mongodb-init-script"
  }
});

// Log successful initialization
print("Funbank MongoDB Event Store initialized successfully:");
print("- Created event_store collection with validation and indexes");
print("- Created snapshots collection for performance optimization");
print("- Created projection_states collection for CQRS read models");  
print("- Created audit_trail collection for banking compliance");
print("- Inserted system initialization event");
print("- Database ready for event sourcing operations");